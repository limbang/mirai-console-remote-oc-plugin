/*
 * Copyright (c) 2025.  limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.model

import kotlinx.serialization.Serializable

/**
 * 标准响应模型，适用于所有API响应
 * @property code 状态码（200表示成功）
 * @property message 状态描述信息
 * @property data 业务数据载体（可为空）
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    /** 判断响应是否成功 */
    fun isSuccess() = code == 200
}
