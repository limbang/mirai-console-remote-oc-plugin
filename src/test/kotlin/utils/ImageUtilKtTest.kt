/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils


import top.limbang.remoteoc.entity.CpuDetail
import top.limbang.remoteoc.entity.Item
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

internal class ImageUtilKtTest {

    private val itemUtil = ItemUtil(javaClass.classLoader.getResource("logback.xml")!!.path.substringBeforeLast("/"))
    private val cpuDetailJson = javaClass.classLoader.getResource("json/CpuDetail2.json")!!.readText()
    private val itemJson = javaClass.classLoader.getResource("json/item1.json")!!.readText()


    @Test
    fun cpuToImage() {
        val cpuDetails = json.decodeFromString<List<CpuDetail>>(cpuDetailJson)
        // 保存最终合并结果
        ImageIO.write(cpuDetails.toImage(itemUtil), "png", File("cpus.png"))
    }

    @Test
    fun itemToImage(){
        val items = json.decodeFromString<List<Item>>(itemJson)
        // 保存最终合并结果
        ImageIO.write(itemUtil.getLocalizedDataList(items).toImage(title = "合成终端"), "png", File("item.png"))
    }
}