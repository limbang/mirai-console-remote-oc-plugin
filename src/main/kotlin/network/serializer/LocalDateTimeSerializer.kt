/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 自定义 [LocalDateTime] 序列化器，用于处理 "yyyy-MM-dd HH:mm:ss" 格式的日期时间字符串。
 *
 * ## 设计说明
 * - **线程安全性**：使用 [DateTimeFormatter]（线程安全）替代 [java.text.SimpleDateFormat]（非线程安全）。
 * - **格式严格性**：序列化/反序列化时严格匹配 "yyyy-MM-dd HH:mm:ss" 格式，避免歧义。
 * - **异常处理**：反序列化失败时抛出 [DateTimeException]，需在调用方捕获处理。
 *
 * ## 使用示例
 * ```kotlin
 * @Serializable
 * data class Event(
 *     @Serializable(with = LocalDateTimeSerializer::class)
 *     val time: LocalDateTime
 * )
 * ```
 */
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(formatter.format(value))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}