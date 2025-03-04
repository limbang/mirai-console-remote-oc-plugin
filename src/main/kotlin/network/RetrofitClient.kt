/*
 * Copyright (c) 2025.  limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import top.limbang.remoteoc.network.annotation.ServerToken
import top.limbang.remoteoc.network.interceptor.AuthInterceptor
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端工厂类
 *
 * ## 功能特性
 * - 自动处理基础URL格式
 * - 集成服务令牌认证系统
 * - 支持调试日志的动态开关
 *
 * @param baseUrl 服务根地址，示例：https://api.example.com/
 * @param token 认证令牌，通过 [AuthInterceptor] 自动注入
 * @param enableLogging 启用请求/响应详细日志（建议仅调试使用）
 * @param json 自定义JSON解析配置，默认忽略未知字段
 *
 * @see ServerToken 认证注解标记机制
 * @throws IllegalArgumentException 接口类不符合Retrofit规范时抛出
 */
class RetrofitClient(
    baseUrl: String,
    token: String,
    enableLogging: Boolean = false,
    json: Json = Json {
        isLenient = true // 宽松模式
        encodeDefaults = true // 编码默认值
        ignoreUnknownKeys = true // 忽略未知的键
    }
) {
    private val logger = LoggerFactory.getLogger(RetrofitClient::class.java)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(token))
            .apply {
                if (enableLogging) {
                    val loggingInterceptor = HttpLoggingInterceptor { message ->
                        logger.debug(message)
                    }
                    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(if (baseUrl.endsWith("/")) "${baseUrl}api/" else "$baseUrl/api/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
    }

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

}