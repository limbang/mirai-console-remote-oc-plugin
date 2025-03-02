/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity

import kotlinx.serialization.Serializable

/**
 * 服务器信息
 *
 * @property url 服务器地址
 * @property token 服务器认证令牌
 */
@Serializable
data class ServerInfo (val url: String, val token: String)