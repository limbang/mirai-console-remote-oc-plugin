/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.limbang.remoteoc.network.model.ActionType.*
import top.limbang.remoteoc.network.model.ActionType.Companion.parseActionType
import top.limbang.remoteoc.network.serializer.LocalDateTimeSerializer
import java.time.LocalDateTime

/**
 * 设备活动信息数据模型
 * @property id 设备唯一标识符，可为空表示匿名设备（如未登录用户设备）
 * @property active 设备操作记录列表，按时间顺序排列
 * @see ActiveRecord 单条操作记录详情
 */
@Serializable
data class DeviceActive(
    val id: String?,
    val active: List<ActiveRecord>,
)

/**
 * 单条设备操作记录
 * @property time 操作发生时间，使用 [LocalDateTimeSerializer] 自定义序列化格式
 * @property type 操作类型，映射为预定义枚举值 [ActionType]
 * @throws SerializationException 当反序列化时间格式不匹配或操作类型未知时抛出
 */
@Serializable
data class ActiveRecord(
    @Serializable(with = LocalDateTimeSerializer::class) val time: LocalDateTime, val type: ActionType
)

/**
 * 设备操作类型枚举
 * @property GET 获取数据操作（映射 JSON 中的 "get"）
 * @property POST 提交数据操作（映射 JSON 中的 "post"）
 * @property UNKNOWN 未知操作类型兜底项（需在业务逻辑中处理）
 *
 * @see parseActionType 安全解析方法，避免枚举未匹配导致的异常
 */
@Serializable
enum class ActionType {
    @SerialName("get")
    GET,

    @SerialName("post")
    POST,

    @SerialName("unknown")
    UNKNOWN;

    /**
     * 安全解析字符串为操作类型，若未匹配则返回 [UNKNOWN]
     * @param value JSON 中原始操作类型字符串
     * @return 对应的枚举实例或 [UNKNOWN]
     */
    companion object {
        fun parseActionType(value: String) = entries.find { it.name == value } ?: UNKNOWN
    }
}