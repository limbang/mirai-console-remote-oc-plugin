/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.annotation

import top.limbang.remoteoc.network.interceptor.AuthInterceptor

/**
 * 服务令牌认证标记注解
 * 被标注的 API 请求会自动在 Header 中添加 x-server-token:<token>
 *
 * @see AuthInterceptor
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServerToken