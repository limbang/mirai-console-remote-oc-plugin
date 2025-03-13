package top.limbang.remoteoc.utils

import top.limbang.remoteoc.utils.IRARUtil.convertToItemJson
import top.limbang.remoteoc.utils.IRARUtil.exportToItemImages
import top.limbang.remoteoc.utils.NESQLUtil.writeItemJsonToFile
import kotlin.test.Test

internal class IRARUtilTest {

    @Test
    fun writeItemJsonToFile() {
        val irarDataList = IRARUtil.readIRARDataList("debug-sandbox/gregtech_item.json")

        // 导出物品图片
        irarDataList.exportToItemImages()

        // 转换为物品元数据
        val itemMetadataMap = irarDataList.convertToItemJson()

        // 写入物品元数据到文件
        itemMetadataMap.writeItemJsonToFile("debug-sandbox/data/top.limbang.RemoteOC/", "gregtech_item.json")
    }

    @Test
    fun exportToItemImages() {
        val irarDataList = IRARUtil.readIRARDataList("debug-sandbox/gregtech_item.json")

        // 导出物品图片
        irarDataList.exportToItemImages()
    }

    @Test
    fun mergeJsonFiles() {
        val itemData1Path = "debug-sandbox/data/top.limbang.RemoteOC/items.json"
        val itemData2Path = "debug-sandbox/data/top.limbang.RemoteOC/gregtech_item.json"

        IRARUtil.mergeJsonFiles(itemData1Path, itemData2Path, "debug-sandbox/data/top.limbang.RemoteOC/merged.json")
    }
}