/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import top.limbang.remoteoc.network.annotation.ServerToken

/**
 * Retrofit 动态认证拦截器
 *
 * 工作原理：
 * 1. 通过 Retrofit 的 Invocation 标签获取接口方法元数据
 * 2. 检查方法是否标注 @ServerToken 注解
 * 3. 动态添加认证头到请求中
 *
 * @property token 服务器认证令牌
 * @see ServerToken 关联的注解定义
 * @throws IllegalStateException 当未正确配置 Retrofit Invocation 标签时
 */
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // 通过 Retrofit 的 Invocation 标签获取方法元数据
        val retrofitMethod = request.tag(Invocation::class.java)?.method()
            ?: throw IllegalStateException("Missing Retrofit Invocation tag")

        if (retrofitMethod.isAnnotationPresent(ServerToken::class.java)) {
            val authedRequest = request.newBuilder()
                .addHeader("x-server-token", token)
                .build()
            return chain.proceed(authedRequest)
        }

        return chain.proceed(request)
    }
}