/*
 * Copyright (c) 2025.  limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */


package top.limbang.remoteoc

import kotlinx.coroutines.cancel
import kotlinx.serialization.encodeToString
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.registerTo
import top.limbang.remoteoc.RemoteOCCompositeCommand.init
import top.limbang.remoteoc.entity.LocalizedData
import top.limbang.remoteoc.listener.ClientListener
import top.limbang.remoteoc.listener.TeamListener
import top.limbang.remoteoc.utils.json


object RemoteOC : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.RemoteOC",
        name = "RemoteOC",
        version = "0.0.1",
    ) {
        author("limbang")
        info("远程控制OC")
    }
) {
    /** 团队合成清单 */
    val teamCraftables = mutableMapOf<String, MutableSet<LocalizedData>>()
    /** 团队合成清单目录 */
    const val TEAM_CRAFTABLES_DIR = "TeamCraftables"

    override fun onEnable() {
        // 加载数据
        RemoteOCData.reload()
        // 确保团队合成目录存在
        resolveDataPath(TEAM_CRAFTABLES_DIR).toFile().mkdirs()
        // 加载团队合成清单
        RemoteOCData.teamClients.keys.forEach { teamId ->
            val file = resolveDataFile("$TEAM_CRAFTABLES_DIR/$teamId.json")
            teamCraftables[teamId] = if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                // 创建空文件并初始化空集合
                file.createNewFile()
                mutableSetOf<LocalizedData>().also {
                    file.writeText(json.encodeToString(it))
                }
            }
        }
        // 初始化命令
        RemoteOCCompositeCommand.register()
        // 初始化 API 服务
        init()

        // 创建事件通道
        GlobalEventChannel.parentScope(this).apply {
            // 注册事件监听器
            TeamListener.registerTo(this)
            ClientListener.registerTo(this)
        }
    }

    override fun onDisable() {
        // 取消事件监听器
        TeamListener.cancel()
        ClientListener.cancel()
        RemoteOCCompositeCommand.unregister()
        // 保存团队合成清单
        RemoteOCData.teamClients.keys.forEach { teamId ->
            resolveDataFile("$TEAM_CRAFTABLES_DIR/$teamId.json").apply {
                writeText(json.encodeToString(teamCraftables[teamId] ?: mutableSetOf()))
            }
        }
    }
}