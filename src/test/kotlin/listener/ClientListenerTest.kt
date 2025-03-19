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
        ImageIO.write(image, "PNG", File("合成终端.png"))
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
        val localizedItem = itemUtil.getLocalizedData(Item("gregtech:gt.metaitem.01", "Titanium Plate", 17028, 0))!!

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
        val item = Item("minecraft:stone", "Stone", 0, 0)

        val result = sendCommandRequest(
            clientId = clientId,
            aeCommand = AeCommand.GetSingleItem(item.name,item.damage),
            serializer = Item.serializer()
        )
        result?.data ?: run { logger.error("❌ 获取物品终端失败"); return@runBlocking }
        val image = itemUtil.getLocalizedDataList(result.data!!).toImage("[{name:\"minecraft:redstone\"},{name:\"minecraft:glass\"}]")
        ImageIO.write(image, "PNG", File("物品终端.png"))
        logger.info(result.data.toString())
    }


    private suspend fun <T> sendCommandRequest(
        clientId: String,
        aeCommand: AeCommand,
        serializer: KSerializer<T>
    ): ResultData<T>? {
        // 创建命令请求
        val taskStatusResponse = try {
            api.executeCommand(
                taskId = "test_${
                    aeCommand.commandString.removePrefix("return ae.").substringBefore("(")
                }",
                command = aeCommand,
                clientId = clientId
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            logger.error(TIMEOUT_ERROR)
            return null
        } ?: return null
        // 处理结果
        val itemList = taskStatusResponse.result!!.first()
        // 构造 ResultData 的序列化器，并传入 T 的序列化器
        val resultDataSerializer = ResultData.serializer(serializer)
        // 返回解析结果
        return json.decodeFromString(resultDataSerializer, itemList)
    }

}