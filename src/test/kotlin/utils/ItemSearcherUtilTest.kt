/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import kotlin.test.Test
import kotlin.test.assertTrue

internal class ItemSearcherUtilTest {

    private val itemUtil = ItemUtil("debug-sandbox/data/top.limbang.RemoteOC")
    private val pinIn = ItemSearcherUtil(itemUtil.localizedItems)

    @Test
    fun searchTest() {
        val result = pinIn.search("xz")
        println(result.joinToString(", ") { it.label })
        assertTrue(result.isNotEmpty())
    }

}