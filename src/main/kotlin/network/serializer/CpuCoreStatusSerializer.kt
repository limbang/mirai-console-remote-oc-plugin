/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import top.limbang.remoteoc.entity.CpuCoreStatus
import top.limbang.remoteoc.utils.json

/**
 * 用于获取简单的 CPU 核心状态信息为数组的情况,自定义序列化器
 *
 */
object CpuCoreStatusSerializer : KSerializer<CpuCoreStatus> {
    private val delegate = CpuCoreStatus.serializer()

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: CpuCoreStatus) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): CpuCoreStatus {
        // 手动检查 JSON 结构
        val input = decoder as? JsonDecoder ?: throw SerializationException("This serializer requires JSON input")
        val element = input.decodeJsonElement()

        return when {
            // 处理空数组的情况
            element is JsonArray && element.isEmpty() -> CpuCoreStatus(
                activeItems = emptyList(),
                storedItems = emptyList(),
                pendingItems = emptyList(),
                active = false,
                busy = false
            )
            // 正常对象的情况
            element is JsonObject -> json.decodeFromJsonElement(delegate, element)
            // 其他异常情况
            else -> throw SerializationException("Expected JSON object or empty array, got ${element::class}")
        }
    }
}