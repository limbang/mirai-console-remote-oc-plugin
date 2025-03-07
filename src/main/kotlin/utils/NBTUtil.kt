/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import net.querz.nbt.io.NBTDeserializer
import net.querz.nbt.io.SNBTUtil
import net.querz.nbt.tag.CompoundTag
import java.io.ByteArrayInputStream
import java.util.*


object NBTUtil {

    /**
     * 解析 NBT 字符串
     *
     * @param nbtString NBT 字符串
     * @return NBT Tag
     */
    fun parseNBTFromString(nbtString: String): CompoundTag {
        return SNBTUtil.fromSNBT(nbtString) as CompoundTag
    }

    /**
     * Base64 字符串 转 NBT Tag
     *
     * @param base64
     * @return
     */
    fun base64StringToCompoundTag(base64: String): CompoundTag {
        // 1. Base64 解码
        val decodedBytes = Base64.getDecoder().decode(base64)
        // 2. 反序列化 NBT
        ByteArrayInputStream(decodedBytes).use {
            val nbtStream = NBTDeserializer().fromStream(it)
            return nbtStream.tag as CompoundTag
        }
    }

    /**
     * 读取 NBT Tag 中的流体名称
     */
    fun CompoundTag.readFluidName(): String? {
        return getString("Fluid")
    }

    /**
     * 读取 NBT Tag 中的 TargetCircuit 的 damage 值
     */
    fun CompoundTag.readTargetCircuitDamage(): Int {
        return getCompoundTag("targetCircuit").getShort("Damage").toInt()
    }

    /**
     * 读取 NBT Tag 中的 TargetCircuit 的 string_id 值
     */
    fun CompoundTag.readTargetCircuitStringId(): String? {
        return getCompoundTag("targetCircuit").getString("string_id")
    }
}