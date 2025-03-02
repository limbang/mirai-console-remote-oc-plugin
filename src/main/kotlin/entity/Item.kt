/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package entity


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 代表游戏内物品的不可变数据模型
 *
 * 适用于物品数据序列化/反序列化场景，包含物品的核心属性标识。
 * 典型使用场景：
 * - 服务端与客户端的物品数据传输
 * - 物品配置文件的持久化存储
 * - 合成配方中的材料项表示
 *
 * @property damage 物品损伤值或元数据标识，用于区分同类物品的不同状态。
 *
 *                示例：
 *                - 0 = 默认状态
 *                - 32767 = 完全损坏（某些模组设定）
 *                - 1-10 = 特殊变种（如不同颜色的羊毛）
 * @property label 物品的本地化显示名称，用于用户界面展示。
 *
 *               示例："高级合金框架", "超能硅岩电池 IV"
 * @property name 物品的唯一命名空间标识符，遵循 [Minecraft命名规范]。
 *
 *              示例：
 *              - "minecraft:diamond_sword" （原版物品）
 *              - "gregtech:wire_coil_32x" （模组物品）
 *              - "ae2:certus_quartz_crystal" （跨模组物品）
 * @property size 物品堆叠数量，取值范围通常为1-64（原版上限），
 *
 *              部分模组物品可突破限制。特殊值：
 *              - 0 = 无效物品
 *              - (-1) = 无限数量（创造模式专用）
 *
 */
@Serializable
data class Item(
    val damage: Int,
    val label: String,
    val name: String,
    val size: Int
)

/**
 * 本地化物品数据实体（包含多语言支持与图形资源路径）
 *
 * 适用于客户端界面渲染和本地化数据存储，实现与具体物品实例的松耦合关联。
 *
 * @property chineseName 物品的中文显示名称
 * @property tooltip 物品的悬浮提示信息集合，每行最大长度建议不超过 40 字符
 * @property imgPath 物品图标的资源路径，遵循资源包规范
 */
@Serializable
data class ItemMetadata(
    @SerialName("zh") val chineseName: String,
    val tooltip: List<String>,
    @SerialName("img_path") val imgPath: String
)

/**
 * 本地化物品组合实体
 *
 * 用于客户端运行时快速访问本地化数据，实现[Item]与本地化资源的桥接。
 *
 * @property item 原始物品数据实例
 * @property chineseName 缓存的中文名称
 * @property imgPath 缓存的图标路径
 */
data class LocalizedItem(
    val item: Item,
    val chineseName: String,
    val imgPath: String
)

/**
 * 代表游戏内流体物质的不可变数据模型
 *
 * @property name 物质的唯一命名空间标识符，遵循 [Minecraft命名规范]。
 * @property label 物质的本地化显示名称，用于用户界面展示。
 * @property amount 物质的数量，单位为 mB（1 mB = 1/1000 mL）。
 */
data class Fluid(
    val name: String,
    val label: String,
    val amount: Int
)

/**
 * 流体物质本地化数据实体
 *
 * 包含流体的物理特性参数与本地化信息，适用于流体管道系统可视化。
 *
 * @property chineseName 流体中文名称
 * @property temperatureKelvin 温度值（单位：K，范围 0-5000）
 * @property luminanceLevel 发光强度（范围 0-15，0表示不发光）
 * @property densityKgPerCubicMeter 密度值（单位：kg/m³，影响流体分层）
 * @property viscosityPascalSecond 粘度系数（单位：Pa·s，影响流动速度）
 */
@Serializable
data class FluidMetadata(
    @SerialName("zh") val chineseName: String,
    @SerialName("Temperature") val temperatureKelvin: Int,
    @SerialName("Luminosity") val luminanceLevel: Int,
    @SerialName("Density") val densityKgPerCubicMeter: Int,
    @SerialName("Viscosity") val viscosityPascalSecond: Int
)

/**
 * 本地化流体物质组合实体
 *
 * 用于客户端运行时快速访问本地化数据，实现[Fluid]与本地化资源的桥接。
 *
 * @property fluid 原始流体数据实例
 * @property chineseName 缓存的中文名称
 * @property imgPath 缓存的图标路径
 */
data class LocalizedFluid(
    val fluid: Fluid,
    val chineseName: String,
    val imgPath: String
)