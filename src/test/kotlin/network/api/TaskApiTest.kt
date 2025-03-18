/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import kotlinx.coroutines.runBlocking
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.model.CommandRequest
import java.io.FileInputStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TaskApiTest {

    private val api: TaskApi
    private val clientId: String

    init {
        val prop = Properties()
        prop.load(FileInputStream("local.properties"))
        val baseUrl = prop.getProperty("baseUrl")
        val token = prop.getProperty("token")
        clientId = prop.getProperty("clientId")
        api = RetrofitClient(baseUrl, token, true).create(TaskApi::class.java)
    }

    @Test
    fun getCommandsTest() = runBlocking {
        val response = api.getCommands(clientId)

        assertEquals(response.code, 200)

        println(response)
    }

    @Test
    fun addCommand() = runBlocking {
        val command = CommandRequest(
            taskId = "123",
            commands = listOf("return ae.getAllCraftables()"),
            clientId = clientId
        )

        val response = api.addCommand(command)

        assertEquals(200, response.code)
        assertEquals("123", response.data?.taskId)
    }

    @Test
    fun getTaskStatusTest() = runBlocking {
        val response = api.getTaskStatus(taskId = "123")

        assertEquals("success", response.message)

        println("任务ID: ${response.data?.taskId}, 任务状态: ${response.data?.status}, 结果: ${response.data?.result}")
    }

}