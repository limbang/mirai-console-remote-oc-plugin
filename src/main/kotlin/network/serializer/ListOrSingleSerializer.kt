/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import top.limbang.remoteoc.utils.json

/**
 * 自定义序列化器，处理 data 字段可能是 List<T> 或单个 T 的情况。
 * 将 JSON 中的数组或对象统一反序列化为 List<T>。
 */
class ListOrSingleSerializer<T>(private val elementSerializer: KSerializer<T>) : KSerializer<List<T>> {
    // 使用 ListSerializer 包装元素序列化器
    private val listSerializer = ListSerializer(elementSerializer)

    // 描述符直接使用 List 的序列化器
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    // 序列化逻辑：直接委托给 ListSerializer
    override fun serialize(encoder: Encoder, value: List<T>) {
        encoder.encodeSerializableValue(listSerializer, value)
    }

    // 反序列化逻辑：处理数组或单个对象
    override fun deserialize(decoder: Decoder): List<T> {
        return when (val input = decoder.decodeSerializableValue(JsonElement.serializer())) {
            // 如果是数组，用 ListSerializer 解析
            is JsonArray -> json.decodeFromJsonElement(listSerializer, input)
            // 如果是对象，解析为单个元素并包装成 List
            else -> listOf(json.decodeFromJsonElement(elementSerializer, input))
        }
    }
}