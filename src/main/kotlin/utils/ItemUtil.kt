/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import entity.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

/**
 * 物品本地化处理工具
 *
 * 通过加载预定义的JSON数据文件，为游戏物品提供本地化名称和图标路径，
 * 支持黑名单过滤和默认值回退机制。
 *
 * @param resourceDir JSON数据文件所在目录路径
 * @param itemJsonName 物品数据文件名（默认：items_GTNH270.json）
 * @param fluidsJsonName 液体数据文件名（默认：fluids_GTNH270.json）
 * @property nameFilters 需要过滤的特殊物品名称集合
 *
 * @throws IllegalArgumentException 当数据文件不存在时抛出
 */
class ItemUtil(
    resourceDir: String,
    itemJsonName: String = "items_GTNH270.json",
    fluidsJsonName: String = "fluids_GTNH270.json",
    private val nameFilters: Set<String> = setOf("ae2fc:fluid_drop", "programmablehatches:prog_circuit")
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
     * 获取单个物品的本地化信息，若名称被过滤，则返回 null。
     *
     * @param item 原始物品对象，需包含名称(name)和损伤值(damage)
     * @return [LocalizedItem] 包含本地化名称和图片路径的对象（可能为null）
     */
    private fun getLocalItem(item: Item): LocalizedItem? {
        // 优先执行过滤检查
        if (nameFilters.contains(item.name)) return null

        val itemMetadata = itemData[item.name]?.get(item.damage.toString())
        return if (itemMetadata != null) {
            LocalizedItem(item, itemMetadata.chineseName, "img/items/${itemMetadata.imgPath}")
        } else {
            logger.warn("未找到物品: $item 采用默认名称和图片")
            LocalizedItem(item, item.label, "img/default.png")
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
     * @see LocalizedItem 返回的数据载体类
     */
    fun getLocalItems(items: List<Item>): List<LocalizedItem> {
        return items.mapNotNull { getLocalItem(it) }
    }

    /**
     * 获取单个液体的本地化信息
     *
     * @param fluid 原始液体对象，需包含名称(name)
     * @return [LocalizedFluid] 包含本地化名称和图片路径的对象
     */
    private fun getLocalFluid(fluid: Fluid): LocalizedFluid {
        val fluidMetadata = fluidData[fluid.name]
        return if (fluidMetadata != null) {
            LocalizedFluid(fluid, fluidMetadata.chineseName, "img/fluids/${fluid.label}.png")
        } else {
            LocalizedFluid(fluid, fluid.label, "img/default.png")
        }
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
     * @see LocalizedFluid 返回的数据载体类
     */
    fun getLocalFluids(fluids: List<Fluid>): List<LocalizedFluid> {
        return fluids.map { getLocalFluid(it) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ItemUtil::class.java)
    }
}