/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

/**
 * 团队信息
 *
 * @property name 团队名称 也是 ID
 * @property captainId 团队队长 ID
 * @property members 成员列表
 * @property invitations 邀请列表
 * @property createTime 创建时间,默认为当前时间
 */
@Serializable
data class Team(
    val name: String,
    val captainId: Long,
    val members: MutableSet<Long> = mutableSetOf(),
    val invitations: MutableSet<Long> = mutableSetOf(),
    val createTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)
