/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.model

import kotlinx.serialization.Serializable
import top.limbang.remoteoc.network.serializer.ListOrSingleSerializer

/**
 * 任务结果数据
 *
 * @param T 数据类型
 * @property data 数据 (可能是 T 或 List<T> 还可能是 null)
 * @property message 提示信息
 */
@Serializable
data class ResultData<T>(
    @Serializable(with = ListOrSingleSerializer::class)
    val data: List<T>? = null,
    val message: String
)