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
 * 服务端信息
 * @property version 服务版本号（遵循语义化版本规范）
 * @property deviceNum 当前注册设备总数
 */
@Serializable
data class Meta(
    val version: String,
    @SerialName("device_num") val deviceNum: Int
)
