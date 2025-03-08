/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils


import top.limbang.remoteoc.entity.LocalizedData
import top.limbang.remoteoc.utils.MinecraftStyle.BACKGROUND_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.BORDER_HIGHLIGHT_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.BORDER_SHADOW_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.ITEM_TEXT_COLOR
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil


/**
 * 把本地化数据转换为终端图像
 *
 * @param title 终端标题 默认为 "终端"
 * @param columnCount 列数 默认为 9
 * @param cellWidth 单元格宽度 默认为 64
 * @param cellHeight 单元格高度 默认为 64
 * @param horizontalPadding 水平边距 默认为 20
 * @param bottomPadding 底部边距 默认为 20
 * @param headerHeight 标题高度 默认为 40
 * @param borderSize 边框大小 默认为 边框总尺寸（由 MinecraftStyle 常量计算得出）
 * @return BufferedImage 终端图像
 */
fun List<LocalizedData>.toImage(
    title: String = "终端",
    columnCount: Int = 9,
    cellWidth: Int = 64,
    cellHeight: Int = 64,
    horizontalPadding: Int = 20,
    bottomPadding: Int = 20,
    headerHeight: Int = 40,
    borderSize: Int = MinecraftStyle.BORDER_WIDTH * 2 + MinecraftStyle.BORDER_HIGHLIGHT_SHADOW_WIDTH * 2
): BufferedImage {

    // 计算网格布局参数
    val rowCount = ceil(size.toDouble() / columnCount).toInt()
    val canvasWidth = (cellWidth * columnCount) + (horizontalPadding * 2) + borderSize
    val canvasHeight = headerHeight + (cellHeight * rowCount) + bottomPadding + borderSize

    val image = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics().apply {
        setRenderingHints(
            mapOf(
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
            )
        )
    }

    // 绘制背景
    g2d.color = BACKGROUND_COLOR
    g2d.fillRect(0, 0, canvasWidth, canvasHeight)

    // 绘制标题文字
    g2d.color = ITEM_TEXT_COLOR
    g2d.font = Font("Microsoft YaHei", Font.PLAIN, 22)
    g2d.drawString(title, horizontalPadding + (borderSize / 2), headerHeight - 10 + (borderSize / 2))

    // 绘制物品格子
    forEachIndexed { index, data ->
        val col = index % columnCount
        val row = index / columnCount
        val x = horizontalPadding + (col * cellWidth) + (borderSize / 2)
        val y = headerHeight + (row * cellHeight) + (borderSize / 2)
        drawTableCell(g2d, data, x, y, cellWidth, cellHeight)
    }

    // 补充空白单元格以保持网格完整性
    val totalCells = rowCount * columnCount
    val emptyCells = totalCells - size
    repeat(emptyCells) { i ->
        val index = size + i
        val col = index % columnCount
        val row = index / columnCount
        val x = horizontalPadding + (col * cellWidth) + (borderSize / 2)
        val y = headerHeight + (row * cellHeight) + (borderSize / 2)
        drawTableCell(g2d, null, x, y, cellWidth, cellHeight)
    }

    // 绘制整体边框效果
    drawBorderEffect(g2d, canvasWidth, canvasHeight)

    g2d.dispose()
    return image
}

/**
 * 绘制单元格
 *
 * @param g Graphics2D
 * @param data 本地化数据
 * @param x 左上角 x 坐标
 * @param y 左上角 y 坐标
 * @param width 宽度
 * @param height 高度
 */
private fun drawTableCell(g: Graphics2D, data: LocalizedData?, x: Int, y: Int, width: Int, height: Int) {
    // 绘制单元格背景
    g.color = MinecraftStyle.ITEM_COLOR
    g.fillRect(x, y, width, height)
    // 绘制边框阴影
    g.color = BORDER_SHADOW_COLOR
    g.fillRect(x, y, width - 3, 3)
    g.fillRect(x, y, 3, height - 3)
    // 绘制边框高光
    g.color = BORDER_HIGHLIGHT_COLOR
    g.fillRect(x + 3, y + height - 3, width - 3, 3)
    g.fillRect(x + width - 3, y + 3, 3, height - 3)

    // 若数据为 null，不需要绘制内容
    if (data == null) return

    val iconSize = if (data.isFluid) 58 else 48

    // 加载并绘制图标
    val icon = try {
        ImageIO.read(File(data.imgPath)).getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH)
    } catch (e: Exception) {
        // 图标加载失败，读取默认图标
        getImage("default.png")!!
    }
    g.drawImage(icon, x + (width - iconSize) / 2, y + (height - iconSize) / 2, null)


    // 绘制数据名称
    g.color = Color.WHITE
    g.font = Font("Microsoft YaHei", Font.PLAIN, 9)
    g.drawString(truncateText(data.name,g.fontMetrics,55), x + 5, y + 2 + g.fontMetrics.ascent)

    // 获取字体度量信息
    g.font = Font("Microsoft YaHei", Font.PLAIN, 12)
    val fm = g.fontMetrics
    val text = if (data.size < 1 && data.isCraftable) "合成" else NumberFormatter.format(data.size)
    val textWidth = fm.stringWidth(text)

    // 计算文本的绘制位置，确保右对齐并留有边距
    val textX = x + width - 8 - textWidth // 右对齐，距离右边 8px
    val textY = y + height - 4 - fm.descent // 确保底部间距 8px

    // 绘制合成或数量
    g.drawString(text, textX, textY)
}
