/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity

import kotlinx.serialization.Serializable



/**
 * CPU 详细信息
 * @property storage 存储容量（单位：B）
 * @property coprocessors 并行处理器数量
 * @property name CPU 名称
 * @property cpu CPU 核心状态
 * @property busy 是否处于忙碌状态
 */
@Serializable
data class CpuDetail(
    val storage: Int,
    val coprocessors: Int,
    val name: String,
    val cpu: CpuCoreStatus,
    val busy: Boolean
)

/**
 * CPU 核心状态
 * @property activeItems 正在合成任务队列
 * @property storedItems 现存任务队列
 * @property pendingItems 计划合成任务队列
 * @property active 是否激活
 * @property busy 是否忙碌
 */
@Serializable
data class CpuCoreStatus(
    val activeItems: List<Item>,
    val storedItems: List<Item>,
    val pendingItems: List<Item>,
    val active: Boolean,
    val busy: Boolean
)

/**
 * CPU 任务队列实体类
 *
 * @property itemName 物品名称
 * @property activeNumber 正在合成数量
 * @property pendingNumber 计划合成数量
 * @property storedNumber 现存数量
 * @property imagePath 图片路径
 */
@Serializable
data class CpuTaskQueue(
    val itemName: String,
    val activeNumber: Int = 0,
    val pendingNumber: Int = 0,
    val storedNumber: Int = 0,
    val imagePath: String
)