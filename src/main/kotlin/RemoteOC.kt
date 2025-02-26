/*
 * Copyright (c) 2025.  limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */


package top.limbang.remoteoc

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

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

    }

    override fun onDisable() {

    }
}