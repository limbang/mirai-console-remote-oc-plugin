/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import retrofit2.http.GET
import top.limbang.remoteoc.network.annotation.ServerToken
import top.limbang.remoteoc.network.model.ApiResponse
import top.limbang.remoteoc.network.model.DeviceActive
import top.limbang.remoteoc.network.model.Meta
import top.limbang.remoteoc.network.model.VersionResponse

/**
 * 服务端信息接口
 */
interface InfoApi {

    /**
     * 获取服务端信息
     *
     */
    @ServerToken
    @GET("info/meta")
    suspend fun getMeta(): ApiResponse<Meta>

    /**
     * 获取服务端版本信息
     */
    @GET("info/version")
    suspend fun getVersion(): ApiResponse<VersionResponse>

    /**
     * 获取客户端列表
     */
    @ServerToken
    @GET("info/devices")
    suspend fun getDevices(): ApiResponse<List<DeviceActive>>
}