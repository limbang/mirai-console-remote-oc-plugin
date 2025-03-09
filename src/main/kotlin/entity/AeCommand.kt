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

    /**
     * 获取所有流体命令
     */
    data object GetAllFluids :
        AeCommand("return ae.getAllFluids()")

    /**
     * 获取所有源质命令
     */
    data object GetAllEssentia :
        AeCommand("return ae.getAllEssentia()")

    /**
     * 获取所有物品命令
     */
    data class GetAllItems(val filter: String?) :
        AeCommand("return ae.getAllItems(${filter?.let { "'$it'" } ?: ""})")

    /**
     * 请求物品命令
     * @param itemName 物品名称
     * @param damage 物品耐久度
     * @param amount 请求数量
     */
    data class RequestItem(val itemName: String, val damage: Int, val amount: Int) :
        AeCommand("return ae.requestItem('$itemName', $damage, $amount)")

    /**
     * 取消物品请求命令
     * @param cpuName CPU名称
     */
    data class CancelCraftingByCpuName(val cpuName: String) :
        AeCommand("return ae.cancelCraftingByCpuName('$cpuName')")
}