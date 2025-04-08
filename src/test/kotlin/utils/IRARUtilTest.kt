package top.limbang.remoteoc.utils

import kotlinx.serialization.encodeToString
import top.limbang.remoteoc.entity.ItemMetadata
import top.limbang.remoteoc.utils.IRARUtil.convertToItemJson
import top.limbang.remoteoc.utils.IRARUtil.exportToItemImages
import top.limbang.remoteoc.utils.NESQLUtil.writeItemJsonToFile
import java.io.File
import kotlin.test.Test

internal class IRARUtilTest {

    @Test
    fun writeItemJsonToFile() {
        val irarDataList = IRARUtil.readIRARDataList("debug-sandbox/debug-sandbox/export/actuallyadditions_item.json")

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
    fun allExportToItemImages() {
        // 遍历文件夹所有json文件
        File("debug-sandbox/export/").listFiles { _, name ->
            name.endsWith(".json")
        }?.forEach {
            IRARUtil.readIRARDataList(it.absolutePath).exportToItemImages()
        }
    }

    /**
     * 1.遍历文件夹所有 json文件，导出物品元数据到对应文件夹的 metadata 文件夹下
     *
     */
    @Test
    fun allWriteItemJsonToFile() {
        File("debug-sandbox/export/").listFiles { _, name ->
            name.endsWith(".json")
        }?.forEach {
            val irarDataList = IRARUtil.readIRARDataList(it.absolutePath)
            val itemMetadataMap = irarDataList.convertToItemJson()
            itemMetadataMap.writeItemJsonToFile(it.parentFile.absolutePath + "metadata/", it.name)
        }
    }

    /**
     * 2.合并所有 metadata 文件夹下 json 文件，生成一个 merged.json 文件
     *
     */
    @Test
    fun allMergeJsonFiles() {
        val exportDir = File("debug-sandbox/exportmetadata/").apply { mkdirs() }
        val mergedFile = File(exportDir, "merged.json")

        // 初始化合并数据（类型安全的结构化数据）
        val mergedData = mutableMapOf<String, MutableMap<String, ItemMetadata>>()

        // 读取目录下所有JSON文件（排除已合并文件）
        exportDir.listFiles { _, name ->
            name.endsWith(".json") && name != "merged.json"
        }?.forEach { file ->
            // 安全读取并合并数据
            file.reader().use { reader ->
                val data: Map<String, Map<String, ItemMetadata>> = json.decodeFromString(reader.readText())
                data.forEach { (namespace, items) ->
                    mergedData.getOrPut(namespace) { mutableMapOf() }.apply {
                        items.forEach { (itemId, metadata) ->
                            // 冲突处理策略：保留最新数据
                            put(itemId, metadata)
                        }
                    }
                }
            }
        }

        // 原子化写入合并结果
        mergedFile.writer().buffered().use { writer ->
            json.encodeToString(mergedData).also {
                writer.write(it)
                writer.flush()
            }
        }
    }

    /**
     * 3.合并单个 json 文件到 merged.json 文件
     *
     */
    @Test
    fun mergeJsonFiles() {
        val itemData1Path = "debug-sandbox/data/top.limbang.RemoteOC/items.json"
        val itemData2Path = "debug-sandbox/exportmetadata/merged.json"

        IRARUtil.mergeJsonFiles(itemData1Path, itemData2Path, "debug-sandbox/data/top.limbang.RemoteOC/merged.json")
    }
}