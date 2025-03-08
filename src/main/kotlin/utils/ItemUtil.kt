/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils


import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.*
import top.limbang.remoteoc.utils.NBTUtil.readFluidName
import top.limbang.remoteoc.utils.NBTUtil.readTargetCircuitDamage
import top.limbang.remoteoc.utils.NBTUtil.readTargetCircuitStringId
import java.io.File

/**
 * 物品本地化处理工具
 *
 * 通过加载预定义的JSON数据文件，为游戏物品提供本地化名称和图标路径，
 * 支持黑名单过滤和默认值回退机制。
 *
 * @param resourceDir JSON数据文件所在目录路径
 * @param itemJsonName 物品数据文件名（默认：items.json）
 * @param fluidsJsonName 液体数据文件名（默认：fluids.json）
 * @throws IllegalArgumentException 当数据文件不存在时抛出
 */
class ItemUtil(
    private val resourceDir: String,
    itemJsonName: String = "items.json",
    fluidsJsonName: String = "fluids.json"
) {

    private val itemData: Map<String, Map<String, ItemMetadata>>
    private val fluidData: Map<String, FluidMetadata>

    init {
        // 检查资源目录是否存在
        val itemFile = File(resourceDir, itemJsonName).takeIf { it.exists() }
            ?: throw IllegalArgumentException("物品数据文件不存在: $resourceDir/$itemJsonName")
        val fluidFile = File(resourceDir, fluidsJsonName).takeIf { it.exists() }
            ?: throw IllegalArgumentException("液体数据文件不存在: $resourceDir/$fluidsJsonName")

        // 加载物品数据,自动关闭流
        itemData = itemFile.reader(Charsets.UTF_8).use { Json.decodeFromString(it.readText()) }
        // 加载液体数据,自动关闭流
        fluidData = fluidFile.reader(Charsets.UTF_8).use { Json.decodeFromString(it.readText()) }
    }

    /**
     * 判断物品是否为液滴
     */
    private fun Item.isFluidDrop() = name == "ae2fc:fluid_drop"

    /**
     * 判断物品是否为多功能舱室 prog_circuit
     */
    private fun Item.isProgrammableCircuit() =
        name == "programmablehatches:prog_circuit" && damage == 0 && hasTag

    /**
     * 判断物品是否为纸张
     */
    private fun isSpecialPaper(item: Item, metadata: ItemMetadata) =
        item.name == "minecraft:paper" && item.label != "Paper" && metadata.localizedName == "纸"

    /**
     * 获取单个物品的本地化信息
     *
     * @param item 原始物品对象，需包含名称(name)和(damage)
     * @return [LocalizedData] 包含本地化名称和图片路径的对象
     */
    fun getLocalizedData(item: Item): LocalizedData {
        // 液滴特殊处理逻辑
        if (item.isFluidDrop()) return handleFluidDrop(item)
        // 常规物品处理流程
        return handleRegularItem(item)
    }

    /**
     * 处理流体液滴物品
     *
     * 实现要点：
     * 1. 从NBT标签解析流体名称
     * 2. 使用流体名称的本地化版本+"液滴"后缀
     * 3. 保持液滴自身材质路径逻辑不变
     */
    private fun handleFluidDrop(item: Item): LocalizedData {
        // 处理缺少tag的情况,使用流体数据文件中的名称
        if (item.tag.isEmpty()) {
            val fluidName = item.label.removePrefix("drop of ").lowercase()
            val fluidMetadata = fluidData[fluidName]
            val fluidLocalizedName = fluidMetadata?.localizedName ?: fluidName
            val fluidImgPath = "$resourceDir/image/${fluidMetadata?.imgPath ?: "default.png"}"
            return LocalizedData(
                id = item.name,
                name = fluidLocalizedName,
                imgPath = fluidImgPath,
                damage = item.damage,
                isCraftable = item.isCraftable,
                size = item.size
            )
        }
        // 从Base64编码的tag解析流体名称
        val compoundTag = NBTUtil.base64StringToCompoundTag(item.tag)
        val fluidName = compoundTag.readFluidName()

        // 获取流体元数据
        val fluidMetadata = fluidData[fluidName]

        // 构建本地化名称（流体名称 + "液滴"后缀）
        val localizedName = "${fluidMetadata?.localizedName ?: "未知"}液滴"

        // 保持原始液滴图标逻辑：从itemData读取图片路径
        val fluidImgPath = itemData[item.name]
            ?.get(item.damage.toString())
            ?.imgPath
            ?.let { "$resourceDir/image/$it" }
            ?: "$resourceDir/image/default.png"

        return LocalizedData(
            id = item.name,
            name = localizedName,
            imgPath = fluidImgPath,
            damage = item.damage,
            isCraftable = item.isCraftable,
            size = item.size
        )
    }

    /**
     * 处理常规物品逻辑
     *
     * 实现要点：
     * 1. 处理多功能舱室特殊NBT结构
     * 2. 获取物品元数据
     * 3. 特殊处理minecraft:paper的显示名称
     */
    private fun handleRegularItem(item: Item): LocalizedData {
        // 解析物品标识（优先处理programmable电路板）
        val (damage, itemId) = resolveItemIdentifier(item)

        // 获取物品元数据
        val meta = itemData[itemId]?.get(damage.toString())
        // 输出警告日志
        if (meta == null) logger.warn("未找到物品: ${item.label} 采用默认名称和图片")

        return LocalizedData(
            id = item.name,
            name = if (meta != null) buildDisplayName(item, meta) else item.label,
            imgPath = "$resourceDir/image/${meta?.imgPath ?: "default.png"}",
            damage = damage,
            isCraftable = item.isCraftable,
            size = item.size
        )
    }

    /**
     * 解析物品唯一标识，处理 programmable 电路板特殊 NBT 结构
     */
    private fun resolveItemIdentifier(item: Item): Pair<Int, String> {
        return if (item.isProgrammableCircuit()) {
            val tag = NBTUtil.base64StringToCompoundTag(item.tag)
            tag.readTargetCircuitDamage() to (tag.readTargetCircuitStringId() ?: item.name)
        } else {
            item.damage to item.name
        }
    }

    /**
     * 构建显示名称，处理paper的特殊情况
     */
    private fun buildDisplayName(item: Item, meta: ItemMetadata): String {
        return if (isSpecialPaper(item, meta)) {
            "${meta.localizedName} (${item.label})"
        } else {
            meta.localizedName
        }
    }


    /**
     * 批量获取本地化物品信息
     *
     * @param items 原始物品列表，元素需包含 name 和 damage 属性
     * @return 过滤后的有效本地化物品列表，元素包含：
     *         - 原始物品引用
     *         - 中文名称（优先使用配置，其次使用物品 label）
     *         - 图标路径（优先使用配置，默认返回 default.png）
     *
     * @see Item 原始物品类定义
     * @see LocalizedData 返回的数据载体类
     */
    @JvmName("getLocalizedDataListFromItems")
    fun getLocalizedDataList(items: List<Item>): List<LocalizedData> {
        return items.map { getLocalizedData(it) }
    }

    /**
     * 获取单个液体的本地化信息
     *
     * @param fluid 原始液体对象，需包含名称(name)
     * @return [LocalizedData] 包含本地化名称和图片路径的对象
     */
    fun getLocalizedData(fluid: Fluid): LocalizedData {
        val fluidMetadata = fluidData[fluid.name]
        return LocalizedData(
            id = fluid.name,
            name = fluidMetadata?.localizedName ?: fluid.label,
            imgPath = "$resourceDir/image/${fluidMetadata?.imgPath ?: "default.png"}",
            damage = 0,
            isCraftable = false,
            size = fluid.amount,
            isFluid = true
        )
    }

    /**
     * 批量获取本地化液体信息
     *
     * @param fluids 原始液体列表，元素需包含 name 属性
     * @return 过滤后的有效本地化液体列表，元素包含：
     *         - 原始液体引用
     *         - 中文名称（优先使用配置，其次使用液体 label）
     *         - 图标路径（默认返回 fluid.png）
     *
     * @see Fluid 原始液体类定义
     * @see LocalizedData 返回的数据载体类
     */
    @JvmName("getLocalizedDataListFromFluids")
    fun getLocalizedDataList(fluids: List<Fluid>): List<LocalizedData> {
        return fluids.map { getLocalizedData(it) }
    }

    /**
     * 获取单个源质的本地化信息
     *
     * @param essentia 原始源质对象，需包含名称(name)
     * @return [LocalizedData] 包含本地化名称和图片路径的对象
     */
    fun getLocalizedData(essentia: Essentia): LocalizedData {
        val name = essentia.name.removePrefix("gaseous").removeSuffix("essentia")
        // 获取物品元数据
        val meta = itemData["thaumcraftneiplugin:$name"]?.get("0")
        return LocalizedData(
            id = essentia.name,
            name = meta?.localizedName?.removePrefix("要素: ") ?: essentia.label,
            imgPath = "$resourceDir/image/${meta?.imgPath ?: "default.png"}",
            damage = 0,
            isCraftable = false,
            size = essentia.amount
        )
    }

    /**
     * 批量获取本地化源质信息
     *
     * @param essentias 原始源质列表，元素需包含 name 属性
     * @return 过滤后的有效本地化源质列表，元素包含：
     *         - 原始源质引用
     *         - 中文名称（优先使用配置，其次使用源质 label）
     *         - 图标路径（默认返回 default.png）
     *
     * @see Essentia 原始源质类定义
     * @see LocalizedData 返回的数据载体类
     */
    @JvmName("getLocalizedDataListFromEssentias")
    fun getLocalizedDataList(essentias: List<Essentia>): List<LocalizedData> {
        return essentias.map { getLocalizedData(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemUtil::class.java)
    }
}