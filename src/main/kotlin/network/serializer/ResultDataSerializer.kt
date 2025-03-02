/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.serializer

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import top.limbang.remoteoc.network.model.ResultData

class ResultDataSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<ResultData<T>> {
    // 定义数据结构描述符
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ResultData") {
        element("data", buildSerialDescriptor("DataList", StructureKind.LIST) )
        element("message", PrimitiveSerialDescriptor("Message", PrimitiveKind.STRING))
    }

    // 反序列化：处理 data 字段为对象或数组
    override fun deserialize(decoder: Decoder): ResultData<T> {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("仅支持 JSON 解码")
        val jsonElement = jsonDecoder.decodeJsonElement()

        val jsonObject = jsonElement.jsonObject
        val message = jsonObject["message"]!!.jsonPrimitive.content

        val dataList = when (val dataElement = jsonObject["data"]!!) {
            is JsonArray -> {
                // 处理数组形式
                dataElement.map { itemElement ->
                    jsonDecoder.json.decodeFromJsonElement(dataSerializer, itemElement)
                }
            }
            else -> {
                // 处理对象形式（单条数据）
                listOf(jsonDecoder.json.decodeFromJsonElement(dataSerializer, dataElement))
            }
        }

        return ResultData(dataList, message)
    }

    // 序列化：始终将 data 输出为数组
    override fun serialize(encoder: Encoder, value: ResultData<T>) {
        require(encoder is JsonEncoder) { "仅支持 JSON 编码" }
        val jsonObject  = buildJsonObject  {
            put("message", encoder.json.encodeToJsonElement(value.message))
            put("data", encoder.json.encodeToJsonElement(value.data)) // 自动处理 List 为数组
        }
        encoder.encodeJsonElement(jsonObject)
    }
}