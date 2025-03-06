/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.AeCommand
import top.limbang.remoteoc.network.api.TaskApi
import top.limbang.remoteoc.network.model.CommandRequest
import top.limbang.remoteoc.network.model.TaskStatusResponse

private val logger = LoggerFactory.getLogger(TaskApi::class.java)

/** 定义JSON序列化器 */
val json: Json = Json {
    isLenient = true // 宽松模式
    encodeDefaults = true // 编码默认值
    ignoreUnknownKeys = true // 忽略未知的键
}

/**
 * 异步执行远程命令并轮询任务状态直至完成或超时
 *
 * @param taskId 任务唯一标识符，用于状态查询
 * @param command 包含实际命令的[AeCommand]对象，通过commandString获取命令文本
 * @param clientId 客户端唯一标识符，用于命令发送
 * @return 最终完成状态的任务数据，当命令提交失败或超时时返回null
 * @throws TimeoutCancellationException 若30秒内未完成将抛出超时异常
 *
 * 实现特性：
 * 1. 使用指数退避策略动态调整轮询间隔（500ms ~ 5000ms）
 * 2. 自动清理远程任务状态（remove=true）
 * 3. 协程作用域内可取消
 */
suspend fun TaskApi.executeCommand(taskId: String, command: AeCommand, clientId: String): TaskStatusResponse? {
    // 发送命令
    addCommand(
        CommandRequest(
            taskId = taskId,
            commands = listOf(command.commandString),
            clientId = clientId
        )
    ).takeIf { it.isSuccess() } ?: return run { logger.error(REQUEST_FAILED);null }

    return withTimeout(30_000) {
        // 轮询任务状态
        var delayMs = 500L // 初始延迟
        val finalResponse = run {
            while (true) {
                delay(delayMs) // 动态延迟（500ms ~ 5s）
                val response = getTaskStatus(taskId, remove = true)
                delayMs = (delayMs * 1.5).coerceAtMost(5000.0).toLong() // 最大间隔5秒
                when {
                    response.data?.status == "completed" -> return@run response.data
                    !response.isSuccess() -> {
                        logger.error(RESULT_FAILED)
                        break
                    }
                }
            }
            null
        }
        return@withTimeout finalResponse
    }
}