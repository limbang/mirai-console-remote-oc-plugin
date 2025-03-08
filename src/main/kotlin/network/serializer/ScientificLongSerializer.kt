/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double

/**
 * 将科学计数法字符串/浮点数强制转换为 Long
 */
object ScientificLongSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ScientificLong", PrimitiveKind.STRING)

    // 序列化时直接输出 Long 值
    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeLong(value)
    }

    // 反序列化时处理科学计数法
    override fun deserialize(decoder: Decoder): Long {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            // 处理 JSON 数字（包括科学计数法）
            is JsonPrimitive -> {
                if (!element.isString) {
                    element.double.toLong() // 先转为 Double，再转 Long
                } else {
                    // 处理字符串形式的数字（如 "2.147e+15"）
                    element.content.toDouble().toLong()
                }
            }
            else -> throw SerializationException("Expected JSON number, got ${element::class}")
        }
    }
}