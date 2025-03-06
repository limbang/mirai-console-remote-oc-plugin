/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

/**
 * 数字格式化工具类
 *
 * 遵循 AE 里面的单位定义：
 * ```
 * 9999 以下的数字，不加单位
 * 10_000L - 9_999_999L，后缀为 k
 * 100_000_000L - 999_999_999L，后缀为 M （四舍五入带一位小数）
 * 1_000_000_000L - 999_999_999_999L，后缀为 G （四舍五入带一位小数）
 * 1_000_000_000_000L - 999_999_999_999_999L，后缀为 T （四舍五入带一位小数）
 * 1_000_000_000_000_000L - 999_999_999_999_999_999L，后缀为 P （四舍五入带一位小数）
 * ```
 *
 */
object NumberFormatter {
    // 单位定义：后缀符号、触发阈值、实际除数（格式化为 value = number / divisor）
    private val units = listOf(
        Triple("P", 1_000_000_000_000_000L, 1_000_000_000_000_000L),
        Triple("T", 1_000_000_000_000L, 1_000_000_000_000L),
        Triple("G", 1_000_000_000L, 1_000_000_000L),
        Triple("M", 1_000_000L, 1_000_000L),
        Triple("k", 10_000L, 1_000L)  // 关键修复：k的触发阈值为10_000，除数为1_000
    )

    fun format(number: Long): String {
        // 小于10_000直接返回原值
        if (number < 10_000) return number.toString()

        // 遍历单位，找到第一个满足阈值的单位
        for ((suffix, threshold, divisor) in units) {
            if (number >= threshold) {
                val value = number.toDouble() / divisor
                return if (value >= 10) {
                    // 当值 >=10 时，直接取整显示（如 11_252 → 11k）
                    "${value.toInt()}$suffix"
                } else {
                    // 当值 <10 时，保留一位小数并移除 .0
                    val formatted = "%.1f".format(value)
                    if (formatted.endsWith(".0")) {
                        "${formatted.substringBefore(".0")}$suffix"
                    } else {
                        "$formatted$suffix"
                    }
                }
            }
        }
        return number.toString()
    }
}