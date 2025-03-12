/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.AeCommand
import top.limbang.remoteoc.entity.CpuDetail
import top.limbang.remoteoc.entity.CraftingData
import top.limbang.remoteoc.entity.Item
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.model.CommandRequest
import top.limbang.remoteoc.network.model.ResultData
import top.limbang.remoteoc.utils.ItemUtil
import top.limbang.remoteoc.utils.TIMEOUT_ERROR
import top.limbang.remoteoc.utils.executeCommand
import top.limbang.remoteoc.utils.json
import java.io.FileInputStream
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TaskApiTest {

    private val api: TaskApi
    private val clientId: String
    private val logger = LoggerFactory.getLogger(TaskApiTest::class.java)
    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")

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

    @Test
    fun getCpuListTest() = runBlocking {
        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_getCpuList",
                command = AeCommand.GetCpuList(includeDetails = true),
                clientId = clientId
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return@runBlocking
        } ?: return@runBlocking

        // 处理结果
        val cpuList = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<CpuDetail>>(cpuList)

        val cpuInfo = result.data!!.withIndex().joinToString(
            separator = "\n",
            prefix = "=== CPU 状态 ===\n",
            postfix = "\n================",
            transform = { cpu ->
                "CPU:${cpu.index + 1}\n" +
                        "并行处理器：${cpu.value.coprocessors}\n" +
                        "存储容量：${cpu.value.storage / 1024} K\n" +
                        "忙碌状态: ${cpu.value.busy}" +
                        (if (cpu.value.cpu.activeItems.isNotEmpty()) {
                            "\n正在执行的物品：\n" + itemUtil.getLocalizedDataList(cpu.value.cpu.activeItems)
                                .joinToString(
                                    separator = "\n",
                                    transform = { "名称：${it.name} 数量：${it.size}" }
                                )
                        } else "") +
                        (if (cpu.value.cpu.storedItems.isNotEmpty()) {
                            "\n待存储的物品：\n" + itemUtil.getLocalizedDataList(cpu.value.cpu.storedItems)
                                .joinToString(
                                    separator = "\n",
                                    transform = { "名称：${it.name} 数量：${it.size}" }
                                )
                        } else "") +
                        (if (cpu.value.cpu.pendingItems.isNotEmpty()) {
                            "\n待处理的物品：\n" + itemUtil.getLocalizedDataList(cpu.value.cpu.pendingItems)
                                .joinToString(
                                    separator = "\n",
                                    transform = { "名称：${it.name} 数量：${it.size}" }
                                )
                        } else "")
            }
        )

        logger.info(cpuInfo)
    }

    @Test
    fun getAllCraftablesTest() = runBlocking {
        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_getAllCraftables",
                command = AeCommand.GetAllCraftables,
                clientId = clientId
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return@runBlocking
        } ?: return@runBlocking

        // 处理结果
        val itemList = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<Item>>(itemList)

        val itemInfo = itemUtil.getLocalizedDataList(result.data!!).joinToString(
            separator = "\n",
            prefix = "=== 可合成清单 ===\n",
            postfix = "\n==============",
            transform = { "物品名称：${it.name} 物品图片路径：${it.imgPath}" }
        )

        logger.info(itemInfo)
    }

    @Test
    fun requestItemTest() = runBlocking {
        // 定义一个物品
        val localizedItem = itemUtil.getLocalizedData(Item("gregtech:gt.metaitem.01", "Titanium Plate", 17028, 0))!!

        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_requestItem",
                command = AeCommand.RequestItem(
                    itemName = localizedItem.id,
                    damage = localizedItem.damage,
                    amount = 1
                ),
                clientId = clientId
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return@runBlocking
        } ?: return@runBlocking

        // 处理结果
        val message = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<CraftingData>>(message)

        // 处理合成结果
        result.data!!.forEach { craftingData ->
            logger.info(
                "物品名称：${localizedItem.name}\n" +
                        "物品数量：${craftingData.item.size}\n" +
                        "正在合成：${craftingData.computing}\n" +
                        "是否取消：${craftingData.canceled.result}\n" +
                        "是否完成：${craftingData.done.result}\n" +
                        "是否失败：${craftingData.failed}"
            )
        }
    }

}