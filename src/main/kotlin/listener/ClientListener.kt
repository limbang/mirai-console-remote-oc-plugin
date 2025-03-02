/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.listener

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import top.limbang.remoteoc.RemoteOCData.teamClients
import top.limbang.remoteoc.RemoteOCData.teams
import top.limbang.remoteoc.entity.Team
import top.limbang.remoteoc.listener.TeamListener.sendMessage
import top.limbang.remoteoc.utils.json

/**
 * 监听操作客户端命令
 */
object ClientListener : SimpleListenerHost() {

    private val BIND_CLIENT_REGEX = """^绑定客户端\s?([a-zA-Z0-9]{2,16})""".toRegex()

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
     * 验证团队和客户端是否存在
     *
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


    }

    /**
     * 获取合成清单
     */
    @EventHandler
    suspend fun GroupMessageEvent.getSynthList() {
        if (!message.contentToString().startsWith("获取合成清单")) return
        // 获取团队信息
        val team = validateTeamAndClient() ?: return

    }
}