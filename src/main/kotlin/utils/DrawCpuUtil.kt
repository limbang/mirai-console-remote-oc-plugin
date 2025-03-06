/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils

import top.limbang.remoteoc.entity.*
import top.limbang.remoteoc.utils.MinecraftStyle.ACTIVE_BACKGROUND_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.BACKGROUND_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.CRAFTING_ITEM_BACKGROUND_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.CRAFTING_ITEM_DIVIDING_LINE_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.ITEM_BORDER_HIGHLIGHT_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.ITEM_BORDER_SHADOW_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.ITEM_TEXT_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.PENDING_BACKGROUND_COLOR
import top.limbang.remoteoc.utils.MinecraftStyle.STORED_BACKGROUND_COLOR
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil

/**
 * 扩展函数：将 CpuCoreStatus 中的物品队列转换为 CpuTaskQueue 列表
 *
 * 实现逻辑：
 * 1. 预缓存阶段：收集并校验所有队列中物品的本地化数据，确保数据一致性。
 * 2. 聚合阶段：对每个队列中的物品根据 (name, damage) 键值进行分组，并统计其 size 总和。
 * 3. 合成阶段：整合所有队列的聚合结果，生成按本地化名称排序的 CpuTaskQueue 列表。
 *
 * @param getLocalItem Lambda 表达式，用于获取物品的本地化数据。
 * @return 按本地化名称排序后的 CpuTaskQueue 列表。
 * @throws IllegalArgumentException 当本地化后的物品数据与原始数据不一致时抛出异常。
 */
fun CpuCoreStatus.toCpuTaskQueues(getLocalItem: (Item) -> LocalizedItem?): List<CpuTaskQueue> {
    // 预缓存阶段：合并所有队列，并校验后建立本地化数据缓存
    val localizedItemCache = sequenceOf(activeItems, storedItems, pendingItems)
        .flatten()
        .mapNotNull { item ->
            getLocalItem(item)?.also { localizedItem ->
                // 防御性校验：确保本地化物品与原始物品数据一致
                require(localizedItem.item.name == item.name && localizedItem.item.damage == item.damage) {
                    "Localized item metadata mismatch! Original: (${item.name}, ${item.damage}), " +
                            "Localized: (${localizedItem.item.name}, ${localizedItem.item.damage})"
                }
            }
        }
        .associateBy { it.item.name to it.item.damage }

    // 聚合阶段：统计各队列中每种物品的总 size
    fun aggregateItemSizes(items: List<Item>): Map<Pair<String, Int>, Long> =
        items.mapNotNull { item -> getLocalItem(item)?.item }
            .groupingBy { it.name to it.damage }
            .fold(0L) { acc: Long, item -> acc + item.size }

    val activeSums = aggregateItemSizes(activeItems)
    val storedSums = aggregateItemSizes(storedItems)
    val pendingSums = aggregateItemSizes(pendingItems)


    // 合成阶段：构建 CpuTaskQueue 列表，并按本地化名称排序
    val allKeys = sequenceOf(activeSums, storedSums, pendingSums)
        .flatMap { it.keys }
        .toSet()

    return allKeys.map { key ->
        // 缓存中必定存在对应的本地化数据
        val sample = localizedItemCache[key]!!
        CpuTaskQueue(
            itemName = sample.chineseName,       // 本地化名称
            activeNumber = activeSums[key] ?: 0,   // 活动队列数量
            pendingNumber = pendingSums[key] ?: 0, // 待处理队列数量
            storedNumber = storedSums[key] ?: 0,   // 存储队列数量
            imagePath = sample.imgPath             // 图标路径
        )
    }.sortedBy { it.itemName }
}

/**
 * 扩展函数：根据 CpuDetail 生成 CPU 状态和任务的图像展示
 *
 * 该方法主要完成以下工作：
 * 1. 根据任务队列数据计算网格布局参数（行数、画布宽高等）。
 * 2. 绘制背景、CPU 状态栏、任务卡片、空白占位格以及各类边框。
 *
 * @param itemUtil 用于获取物品本地化数据的工具类。
 * @param isDrawDetailedCpuStatus  是否绘制详细的 CPU 状态，包括 CPU 名称、状态、存储和处理器核数信息。
 * @param cpuNumber CPU 的编号，用于展示。 默认 0。
 * @param columnCount 任务网格的列数，默认 3 列。
 * @param cellWidth 单个任务单元格的宽度。
 * @param cellHeight 单个任务单元格的高度。
 * @param horizontalPadding 水平边距。
 * @param bottomPadding 底部边距。
 * @param headerHeight 状态栏的高度。
 * @param borderSize 边框总尺寸（由 MinecraftStyle 常量计算得出）。
 * @return 生成的 BufferedImage 图像。
 */
fun CpuDetail.toImage(
    itemUtil: ItemUtil,
    isDrawDetailedCpuStatus: Boolean = true,
    cpuNumber: Int = 0,
    columnCount: Int = 3,
    cellWidth: Int = 206,
    cellHeight: Int = 66,
    horizontalPadding: Int = 20,
    bottomPadding: Int = 20,
    headerHeight: Int = 40,
    borderSize: Int = MinecraftStyle.BORDER_WIDTH * 2 + MinecraftStyle.BORDER_HIGHLIGHT_SHADOW_WIDTH * 2
): BufferedImage {
    // 注意：此处 cpu 为 CpuDetail 内的 CpuCoreStatus 属性
    val taskQueues = cpu.toCpuTaskQueues { itemUtil.getLocalItem(it) }

    // 计算网格布局参数
    val rowCount = ceil(taskQueues.size.toDouble() / columnCount).toInt()
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

    // 绘制 CPU 状态栏
    drawCpuHeader(this, cpuNumber, g2d, canvasWidth, headerHeight, borderSize, isDrawDetailedCpuStatus)

    // 绘制任务卡片
    taskQueues.forEachIndexed { index, queue ->
        val col = index % columnCount
        val row = index / columnCount
        val x = horizontalPadding + (col * cellWidth) + (borderSize / 2)
        val y = headerHeight + (row * cellHeight) + (borderSize / 2)
        drawTableCell(g2d, queue, x, y, cellWidth, cellHeight)
    }

    // 补充空白单元格以保持网格完整性
    val totalCells = rowCount * columnCount
    val emptyCells = totalCells - taskQueues.size
    repeat(emptyCells) { i ->
        val index = taskQueues.size + i
        val col = index % columnCount
        val row = index / columnCount
        val x = horizontalPadding + (col * cellWidth) + (borderSize / 2)
        val y = headerHeight + (row * cellHeight) + (borderSize / 2)
        drawTableCell(g2d, null, x, y, cellWidth, cellHeight)
    }

    // 绘制任务区域外框
    val taskAreaWidth = (cellWidth * columnCount)
    val taskAreaHeight = (cellHeight * rowCount)
    drawTableBorder(
        g2d,
        horizontalPadding + borderSize / 2,
        headerHeight + borderSize / 2,
        taskAreaWidth,
        taskAreaHeight
    )

    // 绘制整体边框效果
    drawBorderEffect(g2d, canvasWidth, canvasHeight)

    g2d.dispose()
    return image
}


/**
 * 绘制 CPU 状态栏，显示 CPU 名称、状态、存储和处理器核数信息
 *
 * @param cpuDetail CpuDetail 对象，包含 CPU 的基本信息。
 * @param cpuNumber CPU 编号，用于当名称为空时显示。
 * @param g Graphics2D 对象，用于绘制。
 * @param width 状态栏区域的宽度。
 * @param height 状态栏区域的高度。
 * @param borderSize 边框尺寸，用于微调文本位置。
 * @param isDrawDetailedCpuStatus 是否绘制详细的 CPU 状态，包括 CPU 名称、状态、存储和处理器核数信息。
 */
private fun drawCpuHeader(
    cpuDetail: CpuDetail,
    cpuNumber: Int,
    g: Graphics2D,
    width: Int,
    height: Int,
    borderSize: Int,
    isDrawDetailedCpuStatus: Boolean
) {
    g.color = ITEM_TEXT_COLOR
    g.font = Font("Microsoft YaHei", Font.BOLD, 16)
    val metrics = g.getFontMetrics(g.font)
    // 计算垂直居中的Y坐标
    val startY = (height - metrics.height) / 2 + metrics.ascent + borderSize / 2

    if (isDrawDetailedCpuStatus) {
        val cpuName = cpuDetail.name.ifEmpty { "CPU # $cpuNumber" }
        val statusText = "状态: ${if (cpuDetail.busy) "忙碌中" else "空闲"}"
        val storageText = "可存储: ${cpuDetail.storage / 1024} KB"
        val coresText = "并行处理单元: ${cpuDetail.coprocessors}"
        val fullText = listOf(cpuName, statusText, storageText, coresText).joinToString("  |  ")

        // 居中绘制 CPU 状态栏文本
        val textWidth = metrics.stringWidth(fullText)
        val startX = (width - textWidth) / 2
        g.drawString(fullText, startX.coerceAtLeast(20), startY)
    } else {
        // 左对齐绘制“合成状态：”文字
        val startX = 28
        g.drawString("合成状态", startX, startY)
    }
}

/**
 * 绘制单元格
 *
 * 当 [queue] 为 null 时，绘制空白单元格；否则绘制包含图标与文本信息的任务单元格。
 *
 * @param g Graphics2D 对象，用于绘制。
 * @param queue CpuTaskQueue 对象，若为 null 则绘制空白单元格。
 * @param x 单元格左上角 x 坐标。
 * @param y 单元格左上角 y 坐标。
 * @param width 单元格宽度。
 * @param height 单元格高度。
 */
private fun drawTableCell(g: Graphics2D, queue: CpuTaskQueue?, x: Int, y: Int, width: Int, height: Int) {
    // 根据状态决定背景色，若 queue 为 null 则使用空白单元格背景色
    val bgColor = when {
        queue == null -> CRAFTING_ITEM_BACKGROUND_COLOR
        queue.activeNumber > 0 -> ACTIVE_BACKGROUND_COLOR
        queue.pendingNumber > 0 -> PENDING_BACKGROUND_COLOR
        queue.storedNumber > 0 -> STORED_BACKGROUND_COLOR
        else -> CRAFTING_ITEM_BACKGROUND_COLOR
    }

    // 绘制单元格背景与边框
    g.color = bgColor
    g.fillRect(x, y, width, height)
    g.color = CRAFTING_ITEM_DIVIDING_LINE_COLOR
    g.stroke = BasicStroke(3f)
    g.drawRect(x, y, width, height)

    // 若 queue 为 null，不需要绘制内容
    if (queue == null) return

    // 加载并绘制图标
    val icon = try {
        ImageIO.read(File(queue.imagePath)).getScaledInstance(48, 48, Image.SCALE_SMOOTH)
    } catch (e: Exception) {
        // 图标加载失败，读取默认图标
        getImage("default.png")!!
    }
    g.drawImage(icon, x + width - 58, y + (height - 48) / 2, null)

    // 绘制物品名称
    g.font = Font("Microsoft YaHei", Font.BOLD, 12)
    val metricsBold = g.getFontMetrics(g.font)
    g.color = ITEM_TEXT_COLOR
    val itemName = truncateText(queue.itemName, metricsBold, width - 10)
    val itemNameX = x + 5
    val itemNameY = y + 2 + metricsBold.ascent
    g.drawString(itemName, itemNameX, itemNameY)

    // 绘制数值信息（右侧对齐）
    g.font = Font("Microsoft YaHei", Font.PLAIN, 12)
    val metricsPlain = g.getFontMetrics(g.font)
    val infoList = listOf(
        "正在合成" to queue.activeNumber,
        "计划合成" to queue.pendingNumber,
        "现存" to queue.storedNumber
    ).filter { it.second > 0 }
    val lineHeight = metricsPlain.ascent
    val textStartY = y + (height - (infoList.size * lineHeight)) / 2 + metricsPlain.ascent
    var textY = textStartY
    val textRightX = x + width - 68

    infoList.forEach { (label, value) ->
        val text = "$label: $value"
        val textWidth = metricsPlain.stringWidth(text)
        val textX = textRightX - textWidth
        g.drawString(text, textX, textY)
        textY += lineHeight
    }
}

/**
 * 绘制任务区域的外框，包括阴影和高光效果
 *
 * @param g Graphics2D 对象，用于绘制。
 * @param x 任务区域左上角的 x 坐标。
 * @param y 任务区域左上角的 y 坐标。
 * @param width 任务区域的宽度。
 * @param height 任务区域的高度。
 */
private fun drawTableBorder(g: Graphics2D, x: Int, y: Int, width: Int, height: Int) {
    g.stroke = BasicStroke(3f)
    // 绘制阴影线
    g.color = ITEM_BORDER_SHADOW_COLOR
    g.drawLine(x, y, x + width, y)
    g.drawLine(x, y, x, y + height)
    // 绘制高光线
    g.color = ITEM_BORDER_HIGHLIGHT_COLOR
    g.drawLine(x + width, y, x + width, y + height)
    g.drawLine(x, y + height, x + width, y + height)
}

/**
 * 扩展函数：绘制 CPU 状态，显示 CPU 的名称、存储数量和并行处理器信息
 *
 * 单元格大小与任务单元格一致（默认 cellWidth=206, cellHeight=66），
 * 每个 CPU 对应一个单元格，所有单元格排列为一列。
 * 顶部留有空白区域，使第一个单元格与任务的第一个单元格水平对齐。
 *
 * @param topPadding 顶部空白区域的高度，建议设置为任务状态栏高度（例如 40）。
 * @param cellWidth 单元格宽度（默认为 206，与任务单元格一致）。
 * @param cellHeight 单元格高度（默认为 66，与任务单元格一致）。
 * @param horizontalPadding 左右边距。
 * @param bottomPadding 底部边距。
 * @param borderSize 边框尺寸，默认为 MinecraftStyle 常量计算得出的值。
 * @return 生成的 BufferedImage 图像。
 */
fun List<CpuDetail>.drawCpuStatus(
    topPadding: Int = 40, // 顶部留白区域（与任务绘制的 headerHeight 保持一致）
    cellWidth: Int = 206,
    cellHeight: Int = 66,
    horizontalPadding: Int = 20,
    bottomPadding: Int = 20,
    borderSize: Int = MinecraftStyle.BORDER_WIDTH * 2 + MinecraftStyle.BORDER_HIGHLIGHT_SHADOW_WIDTH * 2
): BufferedImage {
    // 计算网格布局参数：一行一个 CPU
    val rowCount = this.size
    // 整体画布高度包含顶部空白区域、所有单元格、底部边距和边框
    val canvasWidth = cellWidth + horizontalPadding * 2 + borderSize
    val canvasHeight = topPadding + (rowCount * cellHeight) + bottomPadding + borderSize

    val image = BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB)
    val g = image.createGraphics().apply {
        setRenderingHints(
            mapOf(
                RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
            )
        )
    }

    // 绘制整体背景
    g.color = BACKGROUND_COLOR
    g.fillRect(0, 0, canvasWidth, canvasHeight)

    // 加载图标
    val storageIcon = getImage("storage.png")!!.getScaledInstance(24, 24, Image.SCALE_SMOOTH)
    val processorIcon = getImage("coprocessor.png")!!.getScaledInstance(24, 24, Image.SCALE_SMOOTH)

    // 依次绘制每个 CPU 的状态单元格
    this.forEachIndexed { index, cpuDetail ->
        // 计算单元格左上角位置，增加了 topPadding 以保证顶部留白
        val x = horizontalPadding + borderSize / 2
        val y = topPadding + (index * cellHeight) + borderSize / 2

        // 绘制单元格背景与边框（参考 drawTableCell 的样式）
        g.color = if (cpuDetail.busy) ACTIVE_BACKGROUND_COLOR else CRAFTING_ITEM_BACKGROUND_COLOR
        g.fillRect(x, y, cellWidth, cellHeight)
        g.color = CRAFTING_ITEM_DIVIDING_LINE_COLOR
        g.stroke = BasicStroke(3f)
        g.drawRect(x, y, cellWidth, cellHeight)

        // 绘制 CPU 名称（大字体）
        g.color = ITEM_TEXT_COLOR
        g.font = Font("Microsoft YaHei", Font.BOLD, 18)
        val metricsBold = g.getFontMetrics(g.font)
        val cpuName = "CPU #${cpuDetail.name.ifEmpty { index + 1 }}"
        val nameX = x + 10
        val nameY = y + 5 + metricsBold.ascent
        g.drawString(cpuName, nameX, nameY)

        // 绘制存储和并行处理器信息（同一行）
        g.font = Font("Microsoft YaHei", Font.PLAIN, 14)
        val infoY = y + cellHeight - 10
        val iconSpacing = 10
        val firstTextX = nameX + 24 + iconSpacing
        val secondIconX = firstTextX + 60
        val secondTextX = secondIconX + 24 + iconSpacing
        val iconY = infoY - 22

        g.drawImage(storageIcon, nameX, iconY, null)
        g.drawString("${cpuDetail.storage / 1024} KB", firstTextX, infoY)

        g.drawImage(processorIcon, secondIconX, iconY, null)
        g.drawString("${cpuDetail.coprocessors}", secondTextX, infoY)
    }

    // 绘制整体外框：这里调整 y 坐标为 topPadding + borderSize/2
    drawTableBorder(
        g,
        horizontalPadding + borderSize / 2,
        topPadding + borderSize / 2,
        cellWidth,
        rowCount * cellHeight
    )
    // 绘制整体边框效果
    drawBorderEffect(g, canvasWidth, canvasHeight)

    g.dispose()
    return image
}


/**
 * 批量转换 CPU 详情为图片，并合并为一张大图
 *
 * @param itemUtil 本地化信息获取工具
 * @return 合并后的图片
 */
fun List<CpuDetail>.toImage(itemUtil: ItemUtil): BufferedImage {
    // 查看是否有忙碌的 CPU,若没有则返回 绘制 CPU 状态栏
    val activeCpu = filter { it.busy }
    val cpuStatus = drawCpuStatus()
    if (activeCpu.isEmpty()) return cpuStatus
    // 判断有几个忙碌的 CPU,如果等于 1 则绘制 CPU 状态栏靠左,活动任务靠右的布局
    if (activeCpu.size == 1) {
        val cpuDetailImage = activeCpu.first().toImage(itemUtil = itemUtil, isDrawDetailedCpuStatus = false)
        return cpuStatus.mergeImagesSideBySide(cpuDetailImage)
    }
    // 否则批量绘制带 CPU 状态的任务图片, 并合并为一张大图
    val bufferedImages = mutableListOf<BufferedImage>()
    activeCpu.withIndex().forEach { (index, cpuDetail) ->
        bufferedImages.add(cpuDetail.toImage(itemUtil = itemUtil, cpuNumber = index))
    }
    return bufferedImages.mergeImagesTopToBottom()
}