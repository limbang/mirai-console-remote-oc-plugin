/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.entity

/**
 * 表示可识别的命令类型
 *
 * @property commandString 该命令在请求中的原始字符串形式
 */
sealed class AeCommand(val commandString: String) {

    /**
     * 获取CPU列表命令
     * @param includeDetails 是否包含详细硬件信息
     */
    data class GetCpuList(val includeDetails: Boolean) :
        AeCommand("return ae.getCpuList($includeDetails)")

    /**
     * 获取所有可合成物品命令
     */
    data object GetAllCraftables :
        AeCommand("return ae.getAllCraftables()")


}