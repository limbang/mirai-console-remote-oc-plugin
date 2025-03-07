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
import top.limbang.remoteoc.entity.FluidMetadata
import top.limbang.remoteoc.entity.ItemMetadata
import top.limbang.remoteoc.utils.NBTUtil.readTargetCircuitStringId
import java.io.File
import java.sql.Connection
import java.sql.DriverManager


/**
 * NesqlUtil 类
 *
 * 连接到 HSQLDB 数据库，查询物品表数据并转换为本地化 JSON
 *
 */
object NESQLUtil {

    /**
     * 物品表
     *
     * @property imageFilePath 物品图片路径
     * @property internalName 物品内部名称
     * @property itemDamage 物品 damage
     * @property itemId 物品ID
     * @property localizedName 物品本地化名称
     * @property modId 物品所属模组ID
     * @property tooltip 物品提示信息
     * @property nbt 物品 NBT 数据
     */
    @Serializable
    data class ItemEntry(
        @SerialName("IMAGE_FILE_PATH") val imageFilePath: String,
        @SerialName("INTERNAL_NAME") val internalName: String,
        @SerialName("ITEM_DAMAGE") val itemDamage: Int,
        @SerialName("ITEM_ID") val itemId: Int,
        @SerialName("LOCALIZED_NAME") val localizedName: String,
        @SerialName("MOD_ID") val modId: String,
        @SerialName("TOOLTIP") val tooltip: String,
        @SerialName("NBT") val nbt: String = ""
    )

    /**
     * 流体表实体类
     *
     * @property imageFilePath 流体图片路径
     * @property internalName 流体内部名称
     * @property fluidId 流体ID
     * @property localizedName 流体本地化名称
     * @property modId 流体所属模组ID
     * @property luminosity 流体亮度
     * @property density 流体密度
     * @property viscosity 流体粘度
     * @property gaseous 流体是否气体
     */
    @Serializable
    data class FluidEntry(
        @SerialName("IMAGE_FILE_PATH") val imageFilePath: String,
        @SerialName("INTERNAL_NAME") val internalName: String,
        @SerialName("FLUID_ID") val fluidId: Int,
        @SerialName("LOCALIZED_NAME") val localizedName: String,
        @SerialName("MOD_ID") val modId: String,
        @SerialName("TEMPERATURE") val temperature: Int,
        @SerialName("LUMINOSITY") val luminosity: Int,
        @SerialName("DENSITY") val density: Int,
        @SerialName("VISCOSITY") val viscosity: Int,
        @SerialName("GASEOUS") val gaseous: Boolean
    )


    // 数据库连接信息
    private const val USER = "SA"
    private const val PASSWORD = ""

    /**
     * Json 序列化配置
     */
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * 物品表查询语句
     */
    private val itemQuery = """
        SELECT 
            IMAGE_FILE_PATH, INTERNAL_NAME, ITEM_DAMAGE, 
            ITEM_ID, LOCALIZED_NAME, MOD_ID, TOOLTIP,NBT
        FROM ITEM
    """.trimIndent()

    /**
     * 流体表查询语句
     */
    private val fluidQuery = """
        SELECT 
            IMAGE_FILE_PATH, INTERNAL_NAME, FLUID_ID, 
            LOCALIZED_NAME,MOD_ID, TEMPERATURE, LUMINOSITY, 
            DENSITY, VISCOSITY, GASEOUS
        FROM FLUID
    """.trimIndent()

    init {
        Class.forName("org.hsqldb.jdbc.JDBCDriver") // 加载驱动
    }

    /**
     * 获取数据库连接,使用客户端/服务器模式
     *
     * @param name 数据库别名 (--dbname.0 name)
     * @return 数据库连接
     */
    fun getConnection(name: String): Connection {
        return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9001/$name", USER, PASSWORD)
    }

    /**
     * 获取物品表所有数据
     *
     * @return 物品表所有数据
     */
    fun Connection.getItemEntries(): List<ItemEntry> {
        return createStatement().use { stmt ->
            stmt.executeQuery(itemQuery).use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            ItemEntry(
                                imageFilePath = rs.getString("IMAGE_FILE_PATH"),
                                internalName = rs.getString("INTERNAL_NAME"),
                                itemDamage = rs.getInt("ITEM_DAMAGE"),
                                itemId = rs.getInt("ITEM_ID"),
                                localizedName = rs.getString("LOCALIZED_NAME"),
                                modId = rs.getString("MOD_ID"),
                                tooltip = rs.getString("TOOLTIP"),
                                nbt = rs.getString("NBT")
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * 获取流体表所有数据
     *
     * @return 流体表所有数据
     */
    fun Connection.getFluidEntries(): List<FluidEntry> {
        return createStatement().use { stmt ->
            stmt.executeQuery(fluidQuery).use { rs ->
                buildList {
                    while (rs.next()) {
                        add(
                            FluidEntry(
                                imageFilePath = rs.getString("IMAGE_FILE_PATH"),
                                internalName = rs.getString("INTERNAL_NAME"),
                                fluidId = rs.getInt("FLUID_ID"),
                                localizedName = rs.getString("LOCALIZED_NAME"),
                                modId = rs.getString("MOD_ID"),
                                temperature = rs.getInt("TEMPERATURE"),
                                luminosity = rs.getInt("LUMINOSITY"),
                                density = rs.getInt("DENSITY"),
                                viscosity = rs.getInt("VISCOSITY"),
                                gaseous = rs.getBoolean("GASEOUS")
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * 物品表转换为JSON
     */
    fun List<ItemEntry>.convertToItemJson(): Map<String, Map<String, ItemMetadata>> {
        // Key格式: modId:internalName
        return this.groupBy {
            // 处理 prog_circuit 特殊情况
            val internalName = if (it.internalName == "prog_circuit" && it.nbt.isNotBlank()) {
                NBTUtil.parseNBTFromString(it.nbt).readTargetCircuitStringId()
            } else it.internalName
            "${it.modId}:$internalName"
        }.mapValues { (_, groupEntries) ->
            groupEntries.associate { entry ->
                // 处理 prog_circuit 特殊情况
                val damage = if (entry.internalName == "prog_circuit" && entry.nbt.isNotBlank()) {
                    NBTUtil.parseNBTFromString(entry.nbt).readTargetCircuitStringId()
                } else entry.itemDamage
                // Key格式: damage
                damage.toString() to ItemMetadata(
                    localizedName = entry.localizedName,
                    tooltip = entry.tooltip
                        .split("\n")          // 按换行符分割
                        .map { it.trim() }              // 去除每行首尾空格
                        .filter { it.isNotBlank() },    // 处理空值情况
                    imgPath = entry.imageFilePath
                )
            }
        }
    }

    /**
     * 流体表转换为JSON
     */
    fun List<FluidEntry>.convertToFluidJson(): Map<String, FluidMetadata> {
        return this.associate { fluid ->
            // Key格式: internalName
            fluid.internalName to FluidMetadata(
                localizedName = fluid.localizedName,
                temperature = fluid.temperature,
                luminosity = fluid.luminosity,
                density = fluid.density,
                viscosity = fluid.viscosity,
                imgPath = fluid.imageFilePath
            )
        }
    }

    /**
     * 将物品表数据转换为本地化 JSON 并写入文件
     *
     * @param outputPath 输出目录 (默认: src/test/resources/)
     * @param fileName 文件名 (默认: items.json)
     */
    fun Map<String, Map<String, ItemMetadata>>.writeItemJsonToFile(
        outputPath: String = "src/test/resources/",
        fileName: String = "items.json"
    ) {
        writeJsonFile(outputPath, fileName)
    }

    /**
     * 将流体表数据转换为本地化 JSON 并写入文件
     *
     * @param outputPath 输出目录 (默认: src/test/resources/)
     * @param fileName 文件名 (默认: fluids.json)
     */
    fun Map<String, FluidMetadata>.writeFluidJsonToFile(
        outputPath: String = "src/test/resources/",
        fileName: String = "fluids.json"
    ) {
        writeJsonFile(outputPath, fileName)
    }

    /**
     * 将数据转换为本地化 JSON 并写入文件
     *
     * @param outputPath 输出目录 (默认: src/test/resources/)
     * @param fileName 文件名 (默认: items.json)
     */
    private inline fun <reified T> T.writeJsonFile(
        outputPath: String,
        fileName: String
    ) {
        // 创建输出目录
        val outputDir = File(outputPath).apply { if (!exists()) mkdirs() }

        // 构建文件路径
        val outputFile = outputDir.resolve(fileName)

        try {
            // 序列化并写入文件
            outputFile.writeText(json.encodeToString(this), Charsets.UTF_8)

            println(
                """
                    JSON 文件已成功写入
                    路径：${outputFile.absolutePath}
                    大小：${outputFile.length()} 字节
                """.trimIndent()
            )
        } catch (e: Exception) {
            System.err.println("文件写入失败: ${e.message}")
            outputFile.delete() // 清理无效文件
        }
    }
}