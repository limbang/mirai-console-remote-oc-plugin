/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import top.limbang.remoteoc.entity.ServerInfo
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.api.InfoApi
import top.limbang.remoteoc.network.api.TaskApi
import top.limbang.remoteoc.utils.json

object RemoteOCCompositeCommand : CompositeCommand(
    owner = RemoteOC,
    primaryName = "oc",
    description = "配置远程控制 OC"
) {

    lateinit var infoApi: InfoApi

    lateinit var taskApi: TaskApi

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand("add", "添加")
    @Description("添加需要管理的 RemoteOC")
    suspend fun CommandSender.add(@Name("url") baseUrl: String, @Name("token") token: String) {
        RemoteOCData.remoteOCServer = ServerInfo(baseUrl, token)
        init()
        sendMessage("添加成功")
    }

    /**
     * 初始化 RetrofitClient
     */
    fun init() {
        val remoteOCServer = RemoteOCData.remoteOCServer
        if (remoteOCServer != null) {
            val retrofitClient = RetrofitClient(remoteOCServer.url, remoteOCServer.token, json = json)
            taskApi = retrofitClient.create(TaskApi::class.java)
            infoApi = retrofitClient.create(InfoApi::class.java)
        }
    }
}