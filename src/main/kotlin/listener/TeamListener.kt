/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.listener

import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import top.limbang.remoteoc.RemoteOCData.teams
import top.limbang.remoteoc.entity.Team

/**
 * 团队事件监听
 */
object TeamListener : SimpleListenerHost() {

    private const val QQ_ID_PATTERN = "\\d{6,12}"
    private val CREATE_TEAM_REGEX = """^创建团队\s?([\u4e00-\u9fa5a-zA-Z0-9]{2,16})""".toRegex()
    private val INVITE_MEMBER_REGEX = """^邀请加入\s?@($QQ_ID_PATTERN)""".toRegex()
    private val KICK_MEMBER_REGEX = """^踢出团队\s?@($QQ_ID_PATTERN)""".toRegex()

    /**
     * 创建团队
     *
     * 格式：`创建团队 团队名称`
     */
    @EventHandler
    suspend fun GroupMessageEvent.createTeam() {
        val content = message.contentToString()
        val commandMatch = CREATE_TEAM_REGEX.find(content) ?: return
        val (teamName) = commandMatch.destructured

        // 检查团队名称是否重复
        if (teams.containsKey(teamName)) return run { sendMessage("❌ 团队名称已存在") }
        // 检查成员是否已经在其他团队中
        if (teams.values.any { it.members.contains(sender.id) }) return run { sendMessage("⚠️ 您已加入其他团队") }

        // 创建团队
        val team = Team(name = teamName, captainId = sender.id).apply { members.add(sender.id) }

        // 保存团队
        teams[teamName] = team
        sendMessage("✅ 创建[$teamName]团队成功")
    }

    /**
     * 解散团队
     *
     * 格式：`解散团队`
     */
    @EventHandler
    suspend fun GroupMessageEvent.deleteTeam() {
        if (!message.contentToString().startsWith("解散团队")) return

        // 检查成员是否在团队中
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        val teamName = team.name
        // 检查成员是否是队长
        if (team.captainId != sender.id) return run { sendMessage("⛔ 您不是队长，无法删除团队") }

        // 删除团队
        teams.remove(teamName)
        sendMessage("🗑️ [$teamName]团队已解散")
    }

    /**
     * 邀请成员
     *
     * 格式：`邀请加入 @成员`
     */
    @EventHandler
    suspend fun GroupMessageEvent.inviteMember() {
        val content = message.contentToString()
        val commandMatch = INVITE_MEMBER_REGEX.find(content) ?: return
        val (memberId) = commandMatch.destructured

        // 检查是否在团队中
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        // 检查是否是队长
        if (team.captainId != sender.id) return run { sendMessage("⛔ 您不是队长，无法邀请成员") }
        // 检查被邀请成员是否已经在其他团队中
        val name = group[memberId.toLong()]?.nameCardOrNick ?: "未知成员"
        if (teams.values.any { it.members.contains(memberId.toLong()) }) return run { sendMessage("[$name]已加入其他团队") }

        // 邀请成员
        team.invitations.add(memberId.toLong())
        sendMessage("📩 已向[$name]发送邀请,同意加入[${team.name}]团队请输入“加入团队”")
    }

    /**
     * 加入团队
     *
     * 格式：`加入团队`
     */
    @EventHandler
    suspend fun GroupMessageEvent.joinTeam() {
        if (!message.contentToString().startsWith("加入团队")) return

        // 检查成员是否已经在其他团队中
        if (teams.values.any { it.members.contains(sender.id) }) return run { sendMessage("⚠️ 您已加入其他团队") }
        // 检查成员是否已经被邀请
        val team = teams.values.find { it.invitations.contains(sender.id) }
            ?: return run { sendMessage("❌ 您没有被邀请加入任何团队") }

        // 加入团队
        team.members.add(sender.id)
        // 移除邀请
        team.invitations.remove(sender.id)
        sendMessage("🎉 成功加入[${team.name}]")
    }


    /**
     * 退出团队
     *
     * 格式：`退出团队`
     */
    @EventHandler
    suspend fun GroupMessageEvent.quitTeam() {
        if (!message.contentToString().startsWith("退出团队")) return
        // 检查成员是否在团队中
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        // 检查是否是队长
        if (team.captainId == sender.id) return run { sendMessage("⚠️ 您是队长，无法退出团队,请解散团队") }

        // 退出团队
        team.members.remove(sender.id)
        sendMessage("👋 您已退出[${team.name}]")
    }

    /**
     * 踢出成员
     *
     * 格式：`踢出团队 @成员`
     */
    @EventHandler
    suspend fun GroupMessageEvent.kickMember() {
        val content = message.contentToString()
        val commandMatch = KICK_MEMBER_REGEX.find(content) ?: return
        val (memberId) = commandMatch.destructured


        // 检查是否在团队中
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        // 检查是否是队长
        if (team.captainId != sender.id) return run { sendMessage("⛔ 您不是队长，无法踢出成员") }
        // 检查被踢出成员是否在团队中
        if (!team.members.contains(memberId.toLong())) return run { sendMessage("⚠️ 被踢出成员不在团队中") }
        // 检查踢出成员是否是队长
        if (team.captainId == memberId.toLong()) return run { sendMessage("⚠️ 您不能踢自己") }

        val name = group[memberId.toLong()]?.nameCardOrNick ?: "未知成员"

        // 踢出成员
        team.members.remove(memberId.toLong())
        sendMessage("踢出[$name]成功")
    }

    /**
     * 获取团队列表
     *
     * 格式：`团队列表`
     */
    @EventHandler
    suspend fun GroupMessageEvent.getTeamList() {
        if (!message.contentToString().startsWith("团队列表")) return
        val teamList = teams.keys.joinToString("\n")
        if (teamList.isEmpty()) return run { subject.sendMessage("ℹ️ 暂无团队") }
        val message = "🛡️ 团队列表 🛡️\n$teamList"
        subject.sendMessage(message)
    }

    /**
     * 获取团队信息
     *
     * 格式：`团队信息`
     */
    @EventHandler
    suspend fun GroupMessageEvent.getTeamInfo() {
        if (!message.contentToString().startsWith("团队信息")) return
        // 检查是否在团队中
        val team = teams.values.find { it.members.contains(sender.id) }
            ?: return run { sendMessage("❌ 您不在任何团队中") }
        val message =
            "🛡️ 团队信息 🛡️\n名称：${team.name}\n队长：${group[team.captainId]?.nameCardOrNick ?: "未知"}\n成员：\n${
                team.members.joinToString("\n") { group[it]!!.nameCardOrNick }
            }\n邀请列表：\n${
                team.invitations.joinToString("\n") { group[it]!!.nameCardOrNick }
            }\n创建时间：${team.createTime}"
        sendMessage(message)
    }

    /**
     * 团队指令帮助
     *
     * 格式：`团队帮助`
     */
    @EventHandler
    suspend fun GroupMessageEvent.teamHelp() {
        if (!message.contentToString().startsWith("团队帮助")) return
        val message = "🛠️ 团队指令帮助 🛠️\n" +
                "创建团队：创建团队 团队名称\n" +
                "解散团队：解散团队\n" +
                "邀请成员：邀请加入 @成员\n" +
                "加入团队：加入团队\n" +
                "退出团队：退出团队\n" +
                "踢出成员：踢出团队 @成员\n" +
                "团队列表：团队列表\n" +
                "团队信息：团队信息\n" +
                "团队帮助：团队帮助"
        sendMessage(message)
    }

    /**
     * 发送消息
     *
     * @param message
     */
    suspend fun GroupMessageEvent.sendMessage(message: String) {
        subject.sendMessage(At(sender.id) + message)
    }
}
