/*
 * Copyright (c) 2025.  limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */


package top.limbang.remoteoc

import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.registerTo
import top.limbang.remoteoc.RemoteOCCompositeCommand.init
import top.limbang.remoteoc.listener.TeamListener


object RemoteOC : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.RemoteOC",
        name = "RemoteOC",
        version = "0.0.1",
    ){
        author("limbang")
        info("远程控制OC")
    }
){

    override fun onEnable() {
        // 加载数据
        RemoteOCData.reload()
        // 初始化命令
        RemoteOCCompositeCommand.register()
        // 初始化 API 服务
        init()

        // 创建事件通道
        val eventChannel = GlobalEventChannel.parentScope(this)

        // 注册事件监听器
        TeamListener.registerTo(eventChannel)
    }

    override fun onDisable() {
        // 取消事件监听器
        TeamListener.cancel()
        RemoteOCCompositeCommand.unregister()
    }
}