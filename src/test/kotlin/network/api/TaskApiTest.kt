/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import entity.Item
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.AeCommand
import top.limbang.remoteoc.entity.CpuDetail
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.model.CommandRequest
import top.limbang.remoteoc.network.model.ResultData
import top.limbang.remoteoc.utils.ItemUtil
import top.limbang.remoteoc.utils.TIMEOUT_ERROR
import top.limbang.remoteoc.utils.executeCommand
import java.io.FileInputStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TaskApiTest {

    private val api: TaskApi
    private val clientId: String
    private val logger = LoggerFactory.getLogger(TaskApiTest::class.java)

    init {
        val prop = Properties()
        prop.load(FileInputStream("local.properties"))
        val baseUrl = prop.getProperty("baseUrl")
        val token = prop.getProperty("token")
        clientId = prop.getProperty("clientId")
        api = RetrofitClient(baseUrl, token, true).create(TaskApi::class.java)
    }

    @Test
    fun getCommands() = runBlocking {
        val response = api.getCommands(clientId)

        assertEquals(response.code, 200)

        println(response)
    }

    @Test
    fun addCommand() = runBlocking {
        val command =
            CommandRequest(taskId = "123", commands = listOf("return ae.getAllCraftables()"), clientId = clientId)

        val response = api.addCommand(command)

        assertEquals(200, response.code)
        assertEquals("123", response.data?.taskId)
    }

    @Test
    fun getTaskStatus() = runBlocking {
        val response = api.getTaskStatus("123", true, false)

        assertEquals("success", response.message)

        println("任务ID: ${response.data?.taskId}, 任务状态: ${response.data?.status}, 结果: ${response.data?.result}")
    }

    val itemUtil = ItemUtil(javaClass.classLoader.getResource("logback.xml")!!.path.substringBeforeLast("/"))

    @Test
    fun getCpuList() = runBlocking {
        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_getCpuList",
                command = AeCommand.GetCpuList(includeDetails = true),
                clientId = "limbang"
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return@runBlocking
        } ?: return@runBlocking

        // 处理结果
        val cpuList = taskStatusResponse.result!!.first()

        val result = Json.decodeFromString<ResultData<CpuDetail>>(cpuList)

        val cpuInfo = result.data.withIndex().joinToString(
            separator = "\n",
            prefix = "=== CPU 状态 ===\n",
            postfix = "\n================",
            transform = { "CPU:${it.index + 1} 并行处理器：${it.value.coprocessors} 存储容量：${it.value.storage / 1024} K 忙碌状态: ${it.value.busy}" }
        )

        logger.info(cpuInfo)
    }

    @Test
    fun getAllCraftables() = runBlocking {
        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_getAllCraftables",
                command = AeCommand.GetAllCraftables,
                clientId = "limbang"
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return@runBlocking
        } ?: return@runBlocking

        // 处理结果
        val itemList = taskStatusResponse.result!!.first()

        val result = Json.decodeFromString<ResultData<Item>>(itemList)

        val itemInfo = itemUtil.getLocalItems(result.data).joinToString(
            separator = "\n" ,
            prefix = "=== 可合成清单 ===\n",
            postfix = "\n==============",
            transform = { "物品名称：${it.chineseName} 物品图片路径：${it.imgPath}" }
            )

        logger.info(itemInfo)
    }


}