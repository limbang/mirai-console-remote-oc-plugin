/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.listener

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.KSerializer
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import top.limbang.remoteoc.RemoteOC.dataFolderPath
import top.limbang.remoteoc.RemoteOC.teamCraftables
import top.limbang.remoteoc.RemoteOCCompositeCommand.taskApi
import top.limbang.remoteoc.RemoteOCData
import top.limbang.remoteoc.RemoteOCData.teamClients
import top.limbang.remoteoc.RemoteOCData.teams
import top.limbang.remoteoc.entity.*
import top.limbang.remoteoc.listener.TeamListener.sendMessage
import top.limbang.remoteoc.network.model.ResultData
import top.limbang.remoteoc.utils.*
import java.awt.image.BufferedImage
import kotlin.coroutines.CoroutineContext

/**
 * 监听操作客户端命令
 */
object ClientListener : SimpleListenerHost() {

    // 指令正则
    private val BIND_CLIENT_REGEX = """^绑定客户端\s?([a-zA-Z0-9]{2,16})(?:\s+(true|false))?""".toRegex()
    private val CRAFT_ITEM_REGEX = """^合成\s+([^\s\n]+(?:\s+[^\s\n]+)*?)(?:\s+(\d{1,12}))?$""".toRegex()
    private val CANCEL_CRAFTING_REGEX = """^取消合成\s+(.*)""".toRegex()

    // 物品本地化工具
    private val itemUtil = ItemUtil(dataFolderPath.toString())

    // 合成数量限制
    private const val MAX_CRAFT_AMOUNT = 1_000_000
    private const val DEFAULT_CRAFT_AMOUNT = 1

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        println("捕获到事件处理异常: ${exception.message}")
        exception.printStackTrace()
    }

    /**
     * 绑定客户端
     */
    @EventHandler
    suspend fun GroupMessageEvent.bindClient() {
        val content = message.contentToString()
        val commandMatch = BIND_CLIENT_REGEX.find(content) ?: return
        val (clientName, isSimpleMode) = commandMatch.destructured

        // 检查客户端是否被绑定
        if (teamClients.containsValue(clientName)) return run { sendMessage("❌ 客户端已被绑定") }
        // 检查是否加入团队
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        // 团队客户端模式
        RemoteOCData.isSimpleMode[team.name] = isSimpleMode.lowercase() == "true"

        // 绑定客户端
        teamClients[team.name] = clientName
        sendMessage("✅ 绑定成功")
    }

    /**
     * 解绑客户端
     */
    @EventHandler
    suspend fun GroupMessageEvent.unbindClient() {
        if (!message.contentToString().startsWith("解绑客户端")) return
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }

        // 检查是否是队长
        if (team.captainId != sender.id) return run { sendMessage("⛔ 您不是队长，无法解绑客户端") }
        // 解绑客户端
        teamClients.remove(team.name)
        RemoteOCData.isSimpleMode.remove(team.name)
        sendMessage("✅ 解绑成功")
    }

    /**
     * 客户端帮助
     */
    @EventHandler
    suspend fun GroupMessageEvent.clientHelp() {
        if (!message.contentToString().startsWith("客户端帮助")) return
        sendMessage(
            """
            🛠️ 客户端操作帮助 🛠️
            
            1. 绑定客户端 客户端名称 [是否简单模式] 🏷️绑定客户端到当前所在的团队，
            绑定后即可使用远程控制功能,类型为可选参数，默认为 false , GTNH 外的OC客户端请设置为 true
            2. 解绑客户端 🏷️解绑当前所在的团队的客户端绑定
            3. 获取状态 🏷️获取当前团队的 AE CPU 状态
            4. 合成终端 🏷️获取当前团队的 AE 的可合成物品
            5. 合成 物品名称 [数量] 🏷️合成指定名称的物品，数量为可选参数，默认为 1  
            6. 物品终端 [过滤名称] 🏷️获取当前团队的 AE 的所有物品，可选参数为过滤名称
            7. 流体终端 🏷️获取当前团队的 AE 的所有流体
            8. 源质终端 🏷️获取当前团队的 AE 的所有源质
            9. 取消合成 CPU名称 🏷️取消指定名称的CPU的合成任务(使用[石英切割刀]为CPU命名)
            10. 客户端帮助 🏷️显示客户端操作帮助
            """.trimIndent()
        )
    }

    /**
     * 验证团队和客户端是否存在
     */
    private suspend fun GroupMessageEvent.validateTeamAndClient(): Team? {
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: run {
                sendMessage("❌ 您不在任何团队中")
                return null
            }

        if (!teamClients.contains(team.name)) {
            sendMessage("❌ 请先绑定客户端")
            return null
        }
        return team
    }

    /**
     * 获取 CPU 信息
     */
    @EventHandler
    suspend fun GroupMessageEvent.getCpuInfo() {
        if (!message.contentToString().startsWith("获取状态")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 发送CPU信息请求
        sendCpuInfo(team)
    }

    /**
     * 获取 CPU 信息, 并发送图片
     */
    private suspend fun GroupMessageEvent.sendCpuInfo(team: Team) {
        // 发送CPU信息请求
        val result = sendCommandRequest(
            team = team,
            aeCommand = AeCommand.GetCpuList(RemoteOCData.isSimpleMode[team.name]?.not() ?: true),
            serializer = CpuDetail.serializer()
        )
        result?.data ?: run { sendMessage("❌ 获取CPU信息失败"); return }
        // 发送CPU信息图片
        sendImage(result.data.toImage(itemUtil))
    }

    /**
     * 获取所有可合成物品
     */
    @EventHandler
    suspend fun GroupMessageEvent.getAllCraftables() {
        if (!message.contentToString().startsWith("合成终端")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 发送合成终端请求
        val result = sendCommandRequest(team, AeCommand.GetAllCraftables, Item.serializer())
        result?.data ?: run { sendMessage("❌ 获取合成终端失败"); return }
        // 把合成终端的物品存储到插件数据中
        result.data.forEach {
            teamCraftables.getOrPut(team.name) { mutableSetOf() }.add(itemUtil.getLocalizedData(it))
        }
        // 发送合成终端图片
        sendImage(itemUtil.getLocalizedDataList(result.data).toImage("合成终端"))
    }

    /**
     * 获取所有流体
     */
    @EventHandler
    suspend fun GroupMessageEvent.getAllFluids() {
        if (!message.contentToString().startsWith("流体终端")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 发送流体终端请求
        val result = sendCommandRequest(team, AeCommand.GetAllFluids, Fluid.serializer())
        result?.data ?: run { sendMessage("❌ 获取流体终端失败"); return }
        // 发送流体终端图片
        sendImage(itemUtil.getLocalizedDataList(result.data).toImage("流体终端"))
    }


    /**
     * 获取所有源质
     */
    @EventHandler
    suspend fun GroupMessageEvent.getAllEntities() {
        if (!message.contentToString().startsWith("源质终端")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 发送源质终端请求
        val result = sendCommandRequest(team, AeCommand.GetAllEssentia, Essentia.serializer())
        result?.data ?: run { sendMessage("❌ 获取源质终端失败"); return }
        // 发送源质终端图片
        sendImage(itemUtil.getLocalizedDataList(result.data).toImage("源质终端"))
    }

    /**
     * 合成物品
     */
    @EventHandler
    suspend fun GroupMessageEvent.craftItem() {
        val content = message.contentToString()
        val commandMatch = CRAFT_ITEM_REGEX.find(content) ?: return
        val (itemName, count) = commandMatch.destructured

        // 解析合成数量
        val countInt = count.toIntOrNull() ?: DEFAULT_CRAFT_AMOUNT
        if (countInt !in 1..MAX_CRAFT_AMOUNT) {
            sendMessage("❌ 合成数量无效（1-${MAX_CRAFT_AMOUNT}）")
            return
        }
        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 获取合成物品清单
        val craftItems =
            teamCraftables[team.name] ?: run { sendMessage("❌ 请先发送[合成终端]指令获取可合成物品清单"); return }
        // 查询物品是否可以合成
        val localizedItem =
            craftItems.find { it.name == itemName } ?: run { sendMessage("❌ 该物品无法合成"); return }
        // 发送合成请求
        sendMessage("📤 合成[$itemName*$countInt]正在发送合成请求，请等待结果")

        // 发送合成请求
        val result = sendCommandRequest(
            team = team,
            aeCommand = AeCommand.RequestItem(
                itemName = localizedItem.id,
                damage = localizedItem.damage,
                amount = countInt
            ),
            serializer = CraftingData.serializer()
        )

        // 处理合成结果
        result?.data ?: run { sendMessage("❌ 合成失败：${result?.message ?: "未知原因"}");return }
        result.data.forEach { craftingData ->
            if (craftingData.failed) {
                val failureReason = craftingData.canceled.why ?: "未知原因"
                val failureMessage = when {
                    failureReason.contains("missing resources") -> "❌ 合成失败：${localizedItem.name} 原因：缺少资源"
                    else -> "❌ 合成失败：${localizedItem.name} 原因：$failureReason"
                }
                sendMessage(failureMessage)
            } else {
                sendMessage("📥 [${localizedItem.name}*${countInt}]合成请求已发送，正在获取CPU信息,请等待结果")
                sendCpuInfo(team)
            }
        }
    }

    /**
     * 取消合成
     */
    @EventHandler
    suspend fun GroupMessageEvent.cancelCrafting() {
        val content = message.contentToString()
        val commandMatch = CANCEL_CRAFTING_REGEX.find(content) ?: return
        val (cpuName) = commandMatch.destructured

        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 发送取消合成请求
        val result = sendCommandRequest(team, AeCommand.CancelCraftingByCpuName(cpuName), Item.serializer())
        // 发送结果消息
        sendMessage(result?.message ?: "❌ 取消合成失败：未知原因")
    }

    /**
     * 发送命令请求
     *
     * @param T 命令返回类型
     * @param team 团队信息
     * @param aeCommand AE命令
     * @return 命令返回结果 为空表示请求失败
     */
    private suspend fun <T> GroupMessageEvent.sendCommandRequest(
        team: Team,
        aeCommand: AeCommand,
        serializer: KSerializer<T>
    ): ResultData<T>? {
        // 创建命令请求
        val taskStatusResponse = try {
            taskApi.executeCommand(
                taskId = "${sender.id}_${
                    aeCommand.commandString.removePrefix("return ae.").substringBefore("(")
                }",
                command = aeCommand,
                clientId = teamClients.getValue(team.name)
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            sendMessage(TIMEOUT_ERROR)
            return null
        } ?: return null
        // 处理结果
        val itemList = taskStatusResponse.result!!.first()
        // 构造 ResultData 的序列化器，并传入 T 的序列化器
        val resultDataSerializer = ResultData.serializer(serializer)
        // 返回解析结果
        return json.decodeFromString(resultDataSerializer, itemList)
    }

    /**
     * 上传图片并发送到群里
     *
     * @param bufferedImage 图片缓冲区
     */
    private suspend fun GroupMessageEvent.sendImage(bufferedImage: BufferedImage) {
        val image = subject.uploadImage(bufferedImage.toInputStream())
        subject.sendMessage(image)
    }

}