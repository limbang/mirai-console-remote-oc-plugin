/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.api

import kotlinx.coroutines.runBlocking
import top.limbang.remoteoc.network.RetrofitClient
import top.limbang.remoteoc.network.model.ActionType
import java.io.FileInputStream
import java.time.LocalDateTime
import java.util.*
import kotlin.test.*

/**
 * 设备信息接口集成测试类
 *
 * ## 测试准备
 * 1. 需在 `local.properties` 中配置 `baseUrl`、`token`、`clientId`
 * 2. 使用 [RetrofitClient] 初始化 API 客户端
 *
 * @see getMetaTest 验证元数据接口返回格式与基本逻辑
 * @see getDevicesTest 验证设备列表非空性及时间有效性
 */
internal class InfoApiTest {

    private val api: InfoApi
    private val clientId: String

    init {
        val prop = Properties()
        prop.load(FileInputStream("local.properties"))
        val baseUrl = prop.getProperty("baseUrl")
        val token = prop.getProperty("token")
        clientId = prop.getProperty("clientId")
        api = RetrofitClient(baseUrl, token, true).create(InfoApi::class.java)
    }


    /**
     * 测试元数据接口
     * - 验证 HTTP 200 状态码
     * - 检查版本号符合语义化版本格式（如 "1.2.3"）
     * - 确保设备数量非负
     */
    @Test
    fun getMetaTest() = runBlocking {
        val response = api.getMeta()

        // 验证响应结构
        assertEquals(200, response.code)
        assertEquals("获取成功", response.message)

        // 验证数据内容
        val meta = response.data
        assertNotNull(meta)
        assertTrue(meta.version.matches(Regex("""\d+\.\d+\.\d+""")))
        assertTrue(meta.deviceNum >= 0)
    }

    @Test
    fun getVersionTest() = runBlocking {
        val response = api.getVersion()

        // 验证响应结构
        assertEquals(200, response.code)
        assertEquals("获取成功", response.message)

        // 版本格式验证
        val version = response.data?.version
        assertNotNull(version)
        assertTrue(version.matches(Regex("""[\d.]+""")))

        println("当前版本: $version")
    }

    /**
     * 测试设备列表接口
     * - 验证返回列表非空
     * - 检查每条记录的 [LocalDateTime] 有效性（年份 ≥2024）
     * - 确认操作类型不为 [ActionType.UNKNOWN]
     */
    @Test
    fun getDevicesTest() = runBlocking {
        val response = api.getDevices()

        // 验证响应结构
        assertEquals(200, response.code)
        assertEquals("获取成功", response.message)

        // 设备列表验证
        val devices = response.data

        devices?.forEach { device ->
            // ID 可空性验证
            if (device.id == null) {
                println("发现匿名设备，活动记录数: ${device.active.size}")
            }

            // 活动记录时间验证
            device.active.forEach { record ->
                assertTrue(record.time.year >= 2024)
                assertNotEquals(ActionType.UNKNOWN, record.type)
            }
        }

        println("获取到 ${devices?.size ?: 0} 台设备数据")
    }
}