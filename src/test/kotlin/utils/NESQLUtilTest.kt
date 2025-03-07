/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import top.limbang.remoteoc.utils.NESQLUtil.convertToFluidJson
import top.limbang.remoteoc.utils.NESQLUtil.convertToItemJson
import top.limbang.remoteoc.utils.NESQLUtil.getFluidEntries
import top.limbang.remoteoc.utils.NESQLUtil.getItemEntries
import top.limbang.remoteoc.utils.NESQLUtil.writeFluidJsonToFile
import top.limbang.remoteoc.utils.NESQLUtil.writeItemJsonToFile
import kotlin.test.Test

internal class NESQLUtilTest {

    @Test
    fun test() {
        // 测试导出数据到json文件
        NESQLUtil.getConnection("nesql").use { conn ->
            conn.getItemEntries()
                .convertToItemJson()
                .writeItemJsonToFile()

            conn.getFluidEntries()
                .convertToFluidJson()
                .writeFluidJsonToFile()
        }
    }

}