/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity


import kotlinx.serialization.Serializable

/**
 * 物品的合成数据
 *
 * @property canceled 取消的结果
 * @property computing 是否正在计算
 * @property done 完成的结果
 * @property failed 是否失败
 * @property item 物品的合成数据
 */
@Serializable
data class CraftingData(
    val canceled: Canceled,
    val computing: Boolean,
    val done: Done,
    val failed: Boolean,
    val item: ItemCrafting
)

/**
 * 取消的结果
 *
 * @property why 失败原因
 * @property result 取消的结果
 */
@Serializable
data class Canceled(val why: String? = null,val result: Boolean)

/**
 * 完成的结果
 *
 * @property why 失败原因
 * @property result 完成的结果
 */
@Serializable
data class Done(val why: String? = null, val result: Boolean)

/**
 * 物品的合成数据
 *
 * @property canProvideEnergy 是否可以提供能量
 * @property capacity 容量
 * @property charge 充电
 * @property damage 损坏
 * @property fluid 物品的流体
 * @property hasTag 是否有标签
 * @property label 标签
 * @property maxCharge 最大充电
 * @property maxDamage 最大损坏
 * @property maxSize 最大尺寸
 * @property name 名称
 * @property size 尺寸
 * @property tier 等级
 * @property transferLimit 转移限制
 */
@Serializable
data class ItemCrafting(
    val canProvideEnergy: Boolean? = null,
    val capacity: Int? = null,
    val charge: Int? = null,
    val damage: Int,
    val fluid: FluidAmount? = null,
    val hasTag: Boolean,
    val label: String,
    val maxCharge: Int? = null,
    val maxDamage: Int,
    val maxSize: Int,
    val name: String,
    val size: Long,
    val tier: Int? = null,
    val transferLimit: Int? = null
)

/**
 * 物品的流体
 *
 * @property amount 流体的数量
 */
@Serializable
data class FluidAmount(val amount: Long)