/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import top.limbang.remoteoc.entity.ItemMetadata
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * [IRAR](https://www.mcmod.cn/class/3115.html) 导出数据转换工具类
 *
 */
object IRARUtil {

    private val logger = LoggerFactory.getLogger(IRARUtil::class.java)

    /**
     * IRAR 数据实体
     *
     * @param smallIcon 小图标路径
     * @param creativeTabName 创造标签名称
     * @param largeIcon 大图标路径
     * @param maxStackSize 最大堆叠数量
     * @param oredictList 矿物词典列表
     * @param type 物品类型
     * @param name 物品名称
     * @param englishName 物品英文名称
     * @param maxDurability 最大耐久度
     * @param registerName 注册名称
     * @param metadata 元数据
     * @constructor 创建 IRAR 数据实体
     */
    @Serializable
    data class IRAREntry(
        val smallIcon: String,
        @SerialName("CreativeTabName") val creativeTabName: String,
        val largeIcon: String,
        val maxStackSize: Int,
        @SerialName("OredictList") val oredictList: String,
        val type: String,
        val name: String,
        val englishName: String,
        val maxDurability: Int,
        val registerName: String,
        val metadata: Int
    )

    /**
     * 读取 IRAR 数据文件
     *
     * @param dataPath 数据文件路径
     * @return IRAR 数据列表
     */
    fun readIRARDataList(dataPath: String): List<IRAREntry> {
        // 读取数据文件
        val itemFile = File(dataPath).takeIf { it.exists() }
            ?: throw IllegalArgumentException("数据文件不存在: $dataPath")

        val irarEntries = mutableListOf<IRAREntry>()

        // 逐行读取数据
        itemFile.reader(Charsets.UTF_8).use {
            it.readLines().forEach { line ->
                irarEntries.add(json.decodeFromString<IRAREntry>(line))
            }
        }
        return irarEntries
    }

    /**
     * 解码器
     */
    private val decoder = Base64.getDecoder()

    /**
     * 解码 Base64 编码的图片数据
     *
     * @param base64Str Base64 编码的图片数据
     * @param outputPath 输出图片路径
     */
    private fun decodeBase64Image(base64Str: String, outputPath: String) {
        try {
            // 移除 Base64 头部（如果存在）
            val cleanBase64 = base64Str.replaceFirst("data:image/\\w+;base64,", "")

            // 解码 Base64 字符串
            val imageBytes = decoder.decode(cleanBase64)

            // 写入文件
            Files.write(Paths.get(outputPath), imageBytes)
            logger.info("图片已保存至：$outputPath")
        } catch (e: Exception) {
            logger.error("解码失败：${e.message}")
        }
    }

    /**
     * 转换 IRAR 数据到物品 JSON
     */
    fun List<IRAREntry>.convertToItemJson(): Map<String, Map<String, ItemMetadata>> {
        // Key格式: modId:internalName
        return this.groupBy {
            it.registerName
        }.mapValues { (_, groupEntries) ->
            groupEntries.associate { entry ->
                // Key格式: damage
                entry.metadata.toString() to ItemMetadata(
                    localizedName = entry.name,
                    tooltip = listOf(),    // 处理空值情况
                    imgPath = "item/${entry.registerName.replace(":", "/")}~${entry.metadata}.png"
                )
            }
        }
    }

    /**
     * 导出 IRAR 数据到物品图片
     */
    fun List<IRAREntry>.exportToItemImages() {
        // 遍历 IRAR 数据
        forEach { entry ->
            try {
                val outputPath = "item/${entry.registerName.replace(":", "/")}~${entry.metadata}.png"

                // 创建父目录（包括所有必要层级）
                val outputFile = File(outputPath)
                outputFile.parentFile?.let { parent ->
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw IOException("无法创建目录：${parent.absolutePath}")
                    }
                }

                // 解码大图标
                decodeBase64Image(base64Str = entry.largeIcon, outputPath = outputPath)
            } catch (e: Exception) {
                logger.error("处理条目 ${entry.registerName} 失败：${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 合并物品 JSON
     *
     * @param itemData 待合并的物品数据
     */
    fun Map<String, Map<String, ItemMetadata>>.mergeItemData(itemData: Map<String, Map<String, ItemMetadata>>) =
        // 合并两个嵌套的Map结构
        this.toMutableMap().apply {
            itemData.forEach { (namespace, items) ->
                // 合并外层命名空间
                merge(namespace, items) { oldItems, newItems ->
                    // 合并内层物品条目，新条目覆盖旧条目
                    oldItems + newItems
                }
            }
        }


    /**
     * 合并物品 JSON
     *
     * @param json1Path 第一个 JSON 文件路径
     * @param json2Path 第二个 JSON 文件路径
     * @param outputPath 输出文件路径
     */
    fun mergeJsonFiles(json1Path: String, json2Path: String, outputPath: String) {
        // 读取并解析第一个JSON文件
        val itemData1: Map<String, Map<String, ItemMetadata>> = File(json1Path)
            .reader()
            .use { json.decodeFromString(it.readText()) }

        // 读取并解析第二个JSON文件
        val itemData2: Map<String, Map<String, ItemMetadata>> = File(json2Path)
            .reader()
            .use { json.decodeFromString(it.readText()) }


        // 写入合并后的结果到新文件
        File(outputPath).parentFile.mkdirs()  // 确保目录存在
        File(outputPath).writeText(json.encodeToString(itemData1.mergeItemData(itemData2)))
    }

}