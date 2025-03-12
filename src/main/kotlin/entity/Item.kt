/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity


import kotlinx.serialization.Serializable
import top.limbang.remoteoc.network.serializer.ScientificLongSerializer

/**
 * 代表游戏内物品的不可变数据模型
 *
 * 适用于物品数据序列化/反序列化场景，包含物品的核心属性标识。
 * 典型使用场景：
 * - 服务端与客户端的物品数据传输
 * - 物品配置文件的持久化存储
 * - 合成配方中的材料项表示
 *
 * @property name 物品的唯一命名空间标识符
 *
 *              示例：
 *              - "minecraft:diamond_sword" （原版物品）
 *              - "gregtech:wire_coil_32x" （模组物品）
 *              - "ae2:certus_quartz_crystal" （跨模组物品）
 * @property label 物品的显示名称
 *
 *               示例："高级合金框架", "超能硅岩电池 IV"
 * @property damage 物品损伤值或元数据标识，用于区分同类物品的不同状态
 *
 *                示例：
 *                - 0 = 默认状态
 *                - 32767 = 完全损坏（某些模组设定）
 *                - 1-10 = 特殊变种（如不同颜色的羊毛）
 * @property size 物品堆叠数量，取值范围通常为1-64（原版上限），
 *
 *              部分模组物品可突破限制。特殊值：
 *              - 0 = 无效物品
 *              - (-1) = 无限数量（创造模式专用）
 * @property isCraftable 物品是否可合成，默认为 true
 * @property hasTag 物品是否带有 nbt 标签，默认为 false
 * @property tag 物品的标签，用于区分同类物品的不同属性
 */
@Serializable
data class Item(
    val name: String,
    val label: String,
    val damage: Int,
    val size: Long,
    val isCraftable: Boolean = true,
    val hasTag: Boolean = false,
    val tag: String = ""
)

/**
 * 本地化物品数据实体（包含多语言支持与图形资源路径）
 *
 * 适用于客户端界面渲染和本地化数据存储，实现与具体物品实例的松耦合关联。
 *
 * @property localizedName 物品的本地化显示名称，用于用户界面展示。
 * @property tooltip 物品的悬浮提示信息集合，每行最大长度建议不超过 40 字符
 * @property imgPath 物品图标的资源路径，遵循资源包规范
 */
@Serializable
data class ItemMetadata(
    val localizedName: String,
    val tooltip: List<String>,
    val imgPath: String
)

/**
 * 代表游戏内流体物质的不可变数据模型
 *
 * @property name 物质的唯一命名空间标识符
 * @property label 显示名称
 * @property amount 物质的数量，单位为 mB（1 mB = 1/1000 mL）
 * @property isCraftable 物质是否可合成，默认为 false
 * @property hasTag 物质是否带有 nbt 标签，默认为 false
 * @property tag 物质的标签，用于区分同类物质的不同属性
 */
@Serializable
data class Fluid(
    val name: String,
    val label: String,
    @Serializable(with = ScientificLongSerializer::class)
    val amount: Long,
    val isCraftable: Boolean = false,
    val hasTag: Boolean = false,
    val tag: String = ""
)

/**
 * 流体物质本地化数据实体
 *
 * 包含流体的物理特性参数与本地化信息，适用于流体管道系统可视化。
 *
 * @property localizedName 流体的本地化显示名称，用于用户界面展示。
 * @property temperature 温度值（单位：K，范围 0-5000）
 * @property luminosity 发光强度（范围 0-15，0表示不发光）
 * @property density 密度值（单位：kg/m³，影响流体分层）
 * @property viscosity 粘度系数（单位：Pa·s，影响流动速度）
 * @property imgPath 流体图标的资源路径，遵循资源包规范
 */
@Serializable
data class FluidMetadata(
    val localizedName: String,
    val temperature: Int,
    val luminosity: Int,
    val density: Int,
    val viscosity: Int,
    val imgPath: String
)

/**
 * 源质实体
 *
 * @property name 源质的唯一命名空间标识符
 * @property label 显示名称
 * @property amount 源质的数量
 * @property hasTag 源质是否带有 nbt 标签，默认为 false。
 * @property tag 源质的标签，用于区分同类源质的不同属性。
 */
@Serializable
data class Essentia(
    val name: String,
    val label: String,
    val amount: Long,
    val hasTag: Boolean = false,
    val tag: String = ""
)

/**
 * 本地化数据实体
 *
 * 用于通用绘制，实现 [Item] 、 [Fluid] 与本地化资源的桥接。
 *
 * @property id 唯一命名空间标识符
 * @property name 本地化显示名称，用于用户界面展示。
 * @property imgPath 图标的资源路径
 * @property damage 损伤值或元数据标识，用于区分同类物品的不同状态。
 * @property isCraftable 是否可合成
 * @property size 数量
 * @property isFluid 是否为流体
 */
@Serializable
data class LocalizedData(
    val id: String,
    val name: String,
    val imgPath: String,
    val damage: Int,
    val isCraftable: Boolean,
    val size: Long,
    val isFluid: Boolean = false
)