/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 任务状态响应体
 *
 * @property clientId 客户端唯一标识符
 * @property gzip 是否启用 GZIP 压缩
 * @property taskId 任务唯一标识符
 * @property status 任务状态（如 "completed"）
 * @property result 任务执行结果 JSON 字符串数组，需二次解析（可能为 null）
 * @property createdTime 任务创建时间（ISO 8601 格式）
 * @property pendingTime 任务进入队列时间（可能为 null）
 * @property completedTime 任务完成时间（可能为 null）
 */
@Serializable
data class TaskStatusResponse(
    @SerialName("client_id")val clientId: String,
    val gzip: Boolean,
    val taskId: String,
    val status: String,
    val result: List<String>?,
    @SerialName("created_time") val createdTime: LocalDateTime,
    @SerialName("pending_time") val pendingTime: LocalDateTime?,
    @SerialName("completed_time") val completedTime: LocalDateTime?
)