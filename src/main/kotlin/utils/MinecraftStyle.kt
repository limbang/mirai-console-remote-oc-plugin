/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import java.awt.Color


/**
 * Minecraft 风格
 *
 */
object MinecraftStyle {
    /** 边框颜色 */
    val BORDER_COLOR: Color = Color.BLACK

    /** 边框宽度 */
    const val BORDER_WIDTH: Int = 3

    /** 边框高光颜色 */
    val BORDER_HIGHLIGHT_COLOR: Color = Color.WHITE

    /** 边框阴影颜色 */
    val BORDER_SHADOW_COLOR = Color(0x555555)

    /** 边框高光阴影宽度 */
    const val BORDER_HIGHLIGHT_SHADOW_WIDTH = 6

    /** 背景颜色 */
    val BACKGROUND_COLOR = Color(0xC6C6C6)

    /** 物品格颜色 */
    val ITEM_COLOR = Color(0x8B8B8B)

    /** 物品格边框阴影颜色 */
    val ITEM_BORDER_SHADOW_COLOR = Color(0x373737)

    /** 物品格边框高光颜色 */
    val ITEM_BORDER_HIGHLIGHT_COLOR: Color = Color.WHITE

    /** 合成状态物品分割线颜色 */
    val CRAFTING_ITEM_DIVIDING_LINE_COLOR = Color(0x818181)

    /** 合成状态物品背景颜色 */
    val CRAFTING_ITEM_BACKGROUND_COLOR = Color(0xDBDBDB)

    /** active 背景色颜色 */
    val ACTIVE_BACKGROUND_COLOR = Color(0xA6C699)

    /** pending 背景色颜色 */
    val PENDING_BACKGROUND_COLOR = Color(0xE8E5CA)

    /** stored 背景色颜色 */
    val STORED_BACKGROUND_COLOR = CRAFTING_ITEM_BACKGROUND_COLOR

    /** 物品文字颜色 */
    val ITEM_TEXT_COLOR = Color(0x404040)

}