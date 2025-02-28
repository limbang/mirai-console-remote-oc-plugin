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
 * 任务指令数据响应
 * @property taskId 任务唯一标识符
 * @property commands 待执行的命令列表
 * @property isChunked 标识响应数据是否分块传输
 */
@Serializable
data class TaskCommandResponse(

    val taskId: String,

    val commands: List<String>,

    @SerialName("is_chunked")
    val isChunked: Boolean
)