package listener

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.AeCommand
import top.limbang.remoteoc.entity.CpuDetail
import top.limbang.remoteoc.entity.CraftingData
import top.limbang.remoteoc.entity.Item
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.api.TaskApi
import top.limbang.remoteoc.network.api.TaskApiTest
import top.limbang.remoteoc.network.model.ResultData
import top.limbang.remoteoc.utils.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.Test


internal class ClientListenerTest {

    private val logger = LoggerFactory.getLogger(TaskApiTest::class.java)
    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")
    private val itemSearcherUtil = ItemSearcherUtil(itemUtil.localizedItems)

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
    fun getCpuListTest() = runBlocking {

        val result = sendCommandRequest(
            clientId = clientId,
            aeCommand = AeCommand.GetCpuList(includeDetails = true),
            serializer = CpuDetail.serializer()
        )
        result?.data ?: run { logger.error("❌ 获取CPU信息失败"); return@runBlocking }
        // 发送流体终端图片
        val image = result.data!!.toImage(itemUtil)
        ImageIO.write(image, "PNG", File("CPU状态.png"))
        logger.info(result.data.toString())
    }

    @Test
    fun getAllCraftablesTest() = runBlocking {

        val result = sendCommandRequest(
            clientId = clientId,
            aeCommand = AeCommand.GetAllCraftables,
            serializer = Item.serializer()
        )
        result?.data ?: run { logger.error("❌ 获取合成终端失败"); return@runBlocking }
        val image = itemUtil.getLocalizedDataList(result.data!!).toImage("合成终端")
        ImageIO.write(image, "PNG", File("合成终端.png"))
        logger.info(result.data.toString())
    }

    @Test
    fun requestItemTest() = runBlocking {
        // 定义一个物品
        val localizedItem = itemUtil.getLocalizedData(Item("gregtech:gt.metaitem.01", "Titanium Plate", 17028, 0))

        val result = sendCommandRequest(
            clientId = clientId,
            aeCommand = AeCommand.RequestItem(
                itemName = localizedItem.id,
                damage = localizedItem.damage,
                amount = 1
            ),
            serializer = CraftingData.serializer()
        )
        result?.data ?: run { logger.error("❌ 合成请求失败"); return@runBlocking }
        logger.info(result.data.toString())
    }

    @Test
    fun getItemsTest() = runBlocking {
        val commands = mutableListOf<AeCommand>()
        itemSearcherUtil.search("电路").forEach {
            commands.add(AeCommand.GetAllItems(it))
        }
        val result = sendCommandRequest(
            clientId = clientId,
            aeCommands = commands,
            serializer = Item.serializer()
        )
        result?.data ?: run { logger.error("❌ 获取物品终端失败"); return@runBlocking }
        val image = itemUtil.getLocalizedDataList(result.data!!).toImage("物品终端")
        ImageIO.write(image, "PNG", File("物品终端.png"))
        logger.info(result.data.toString())
    }

    private suspend fun <T> sendCommandRequest(
        clientId: String,
        aeCommand: AeCommand,
        serializer: KSerializer<T>
    ): ResultData<T>? = sendCommandRequest(clientId, listOf(aeCommand), serializer)

    private suspend fun <T> sendCommandRequest(
        clientId: String,
        aeCommands: List<AeCommand>,
        serializer: KSerializer<T>
    ): ResultData<T>? {
        // 执行命令
        val results = executeCommands(clientId, aeCommands) ?: return null
        // 构建反序列化器
        val resultDataSerializer = ResultData.serializer(serializer)

        // 处理多结果合并逻辑
        return results
            .map { json.decodeFromString(resultDataSerializer, it) }
            .let { resultDataList ->
                ResultData(
                    // 合并所有data集合，保留非空结果
                    data = resultDataList.flatMap { it.data.orEmpty() }.takeIf { it.isNotEmpty() },
                    // 取第一个有效消息
                    message = resultDataList.first().message
                )
            }
    }

    private suspend fun executeCommands(clientId: String, commands: List<AeCommand>): List<String>? {
        return try {
            // 生成唯一任务ID（基于第一个命令和用户ID）
            val taskId = commands.first().generateTaskId("test")
            // 执行命令
            api.executeCommand(
                taskId = taskId,
                commands = commands,
                clientId = clientId
            )?.result
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            null
        }
    }
}