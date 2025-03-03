/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.listener

import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.buildForwardMessage
import top.limbang.remoteoc.RemoteOC.dataFolderPath
import top.limbang.remoteoc.RemoteOCCompositeCommand.taskApi
import top.limbang.remoteoc.RemoteOCData.teamClients
import top.limbang.remoteoc.RemoteOCData.teamCraftItem
import top.limbang.remoteoc.RemoteOCData.teams
import top.limbang.remoteoc.entity.*
import top.limbang.remoteoc.listener.TeamListener.sendMessage
import top.limbang.remoteoc.network.model.ResultData
import top.limbang.remoteoc.utils.*

/**
 * 监听操作客户端命令
 */
object ClientListener : SimpleListenerHost() {

    private val BIND_CLIENT_REGEX = """^绑定客户端\s?([a-zA-Z0-9]{2,16})""".toRegex()
    private val CRAFT_ITEM_REGEX = """^合成\s+([^\s\n]+(?:\s+[^\s\n]+)*?)(?:\s+(\d{1,12}))?$""".toRegex()
    private val itemUtil = ItemUtil(dataFolderPath.toString())

    /**
     * 绑定客户端
     */
    @EventHandler
    suspend fun GroupMessageEvent.bindClient() {
        val content = message.contentToString()
        val commandMatch = BIND_CLIENT_REGEX.find(content) ?: return
        val (clientName) = commandMatch.destructured

        // 检查客户端是否被绑定
        if (teamClients.containsValue(clientName)) return run { sendMessage("❌ 客户端已被绑定") }
        // 检查是否加入团队
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }

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
        sendMessage("✅ 解绑成功")
    }

    /**
     * 客户端帮助
     */
    @EventHandler
    suspend fun GroupMessageEvent.clientHelp() {
        if (!message.contentToString().startsWith("客户端帮助")) return
        sendMessage(
            "\n🛠️ 客户端操作帮助 🛠️\n" +
                    "1. 绑定客户端：绑定客户端 客户端名称 🏷️绑定客户端到当前所在的团队，绑定后即可使用远程控制功能。\n" +
                    "2. 解绑客户端：解绑客户端 🏷️解绑当前所在的团队的客户端绑定。\n" +
                    "3. 获取CPU信息：获取CPU信息 🏷️获取当前所在团队的CPU信息。\n" +
                    "4. 获取合成清单：获取合成清单 🏷️获取当前所在团队的可合成清单。\n" +
                    "5. 合成物品: 合成 物品名称 [数量] 🏷️合成指定名称的物品，数量为可选参数，默认为 1。"
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
        if (!message.contentToString().startsWith("获取CPU信息")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return

        // 创建命令请求
        val taskStatusResponse = try {
            taskApi.executeCommand(
                taskId = "${sender.id}_getCpuList",
                command = AeCommand.GetCpuList(includeDetails = true),
                clientId = teamClients.getValue(team.name)
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            sendMessage(TIMEOUT_ERROR)
            return
        } ?: return

        // 处理结果
        val cpuList = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<CpuDetail>>(cpuList)

        val bufferedImage = result.data.toImage(itemUtil)
        val image = subject.uploadImage(bufferedImage.toInputStream())
        subject.sendMessage(image)
    }

    /**
     * 获取合成清单
     */
    @EventHandler
    suspend fun GroupMessageEvent.getAllCraftables() {
        if (!message.contentToString().startsWith("获取合成清单")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return

        // 创建命令请求
        val taskStatusResponse = try {
            taskApi.executeCommand(
                taskId = "${sender.id}_getAllCraftables",
                command = AeCommand.GetAllCraftables,
                clientId = teamClients.getValue(team.name)
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            sendMessage(TIMEOUT_ERROR)
            return
        } ?: return

        // 处理结果
        val itemList = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<Item>>(itemList)

        val itemInfo = itemUtil.getLocalItems(result.data).joinToString(
            separator = "\n",
            prefix = "\n=== 可合成清单 ===\n",
            postfix = "\n===合成物品发送格式：合成 物品名称 数量===",
            transform = {
                teamCraftItem.getOrPut(team.name) { mutableListOf() }.add(it) // 把合成清单存储到插件数据中
                "物品名称：${it.chineseName}"
            }
        )

        val forwardMessage = buildForwardMessage() {
            itemInfo.lineSequence() // 按换行符分割成行序列（兼容不同系统）
                .chunked(20)   // 按 20 行分组
                .map { it.joinToString("\n") } // 将每组合并回字符串
                .toList().forEach { message ->
                    bot named bot.nick says message
                }
        }
        subject.sendMessage(forwardMessage)

    }

    /**
     * 合成物品
     */
    @EventHandler
    suspend fun GroupMessageEvent.craftItem() {
        val content = message.contentToString()
        val commandMatch = CRAFT_ITEM_REGEX.find(content) ?: return
        val (itemName, count) = commandMatch.destructured

        // 获取团队信息
        val team = validateTeamAndClient() ?: return
        // 获取合成清单
        val craftItems = teamCraftItem[team.name] ?: run { sendMessage("❌ 请先获取合成清单"); return }
        // 查询物品是否可以合成
        val localizedItem =
            craftItems.find { it.chineseName == itemName } ?: run { sendMessage("❌ 该物品无法合成"); return }
        // 转换数量
        val countInt = if (count.isEmpty()) 1 else count.toInt()

        // 创建命令请求
        val taskStatusResponse = try {
            taskApi.executeCommand(
                taskId = "${sender.id}_requestItem",
                command = AeCommand.RequestItem(
                    itemName = localizedItem.item.name,
                    damage = localizedItem.item.damage,
                    amount = countInt
                ),
                clientId = teamClients.getValue(team.name)
            )
        } catch (e: TimeoutCancellationException) {
            // 处理超时
            sendMessage(TIMEOUT_ERROR)
            return
        } ?: return

        // 处理结果
        val message = taskStatusResponse.result!!.first()

        val result = json.decodeFromString<ResultData<CraftingData>>(message)

        // 处理合成结果
        result.data.forEach { craftingData ->
            sendMessage("✅ ${localizedItem.chineseName}合成进行中，请稍后查询结果\n")
        }
    }
}