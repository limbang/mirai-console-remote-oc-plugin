/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 任务命令请求
 * @property taskId 任务类型标识符
 * @property commands 命令列表
 * @property clientId 客户端唯一标识
 */
@Serializable
data class CommandRequest(
    @SerialName("task_id") val taskId: String,
    val commands: List<String>,
    @SerialName("client_id") val clientId: String
)