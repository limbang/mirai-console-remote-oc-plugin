/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc


import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.limbang.remoteoc.entity.ServerInfo
import top.limbang.remoteoc.entity.Team

/**
 * 插件数据存储
 *
 */
object RemoteOCData : AutoSavePluginData("remote-oc") {

    @ValueDescription("存储团队信息")
    val teams: MutableMap<String, Team> by value()

    @ValueDescription("存储团队绑定客户端ID")
    val teamClients: MutableMap<String, String> by value()

    @ValueDescription("存储群对应的 Remote-OC 服务器信息")
    var remoteOCServer: ServerInfo? by value(null)
}