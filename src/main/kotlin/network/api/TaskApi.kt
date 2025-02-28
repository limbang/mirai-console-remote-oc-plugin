/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import retrofit2.http.*
import top.limbang.remoteoc.network.annotation.ServerToken
import top.limbang.remoteoc.network.model.*

/**
 * 任务相关API接口
 */
interface TaskApi {

    /**
     * 获取任务中的指令，返回第一个处于 READY 状态的任务
     *
     * @param clientId 客户端 ID
     * @return
     */
    @ServerToken
    @GET("task/get")
    suspend fun getCommands(
        @Header("x-client-id") clientId: String? = null
    ): ApiResponse<TaskCommandResponse?>

    /**
     * 新建任务，可自定义 taskId，否则返回随机 taskId
     *
     * @param body
     * @return 任务 ID
     */
    @ServerToken
    @POST("task/add")
    suspend fun addCommand(
        @Body body: CommandRequest
    ): ApiResponse<TaskIdResponse?>

    /**
     * 获取指定 task_id 的任务状态
     *
     * @param taskId 任务 ID
     * @param remove 如果任务为完成状态是否删除
     * @param useGzip 对 result 进行 gzip 压缩并返回 base64 编码
     * @return 任务状态
     */
    @ServerToken
    @GET("task/status")
    suspend fun getTaskStatus(
        @Query("task_id") taskId: String,
        @Query("remove") remove: Boolean = true,
        @Query("use_gzip") useGzip: Boolean = false,
    ): ApiResponse<TaskStatusResponse?>

}