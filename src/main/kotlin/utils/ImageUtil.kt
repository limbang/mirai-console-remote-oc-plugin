/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils


import top.limbang.remoteoc.entity.*
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.ceil


/**
 * 将 CPU 核心状态转换为任务队列统计视图
 *
 * 本方法通过聚合三个物品队列（active/stored/pending）中的有效物品数量，生成以本地化名称为维度的统计视图。
 * 过滤逻辑通过 [getLocalItem] 函数实现，返回 null 的物品将被排除在统计结果之外。
 *
 * @param getLocalItem 本地化信息获取函数，需处理以下逻辑：
 *                     - 返回 [LocalizedItem]：表示有效物品
 *                     - 返回 null：表示物品被过滤或无效
 * @return 按物品本地化名称聚合的任务队列统计列表，包含以下特征：
 *         - 列表按中文名称升序排列
 *         - 仅包含未被过滤的有效物品
 *         - 各队列数量基于物品的 (name, damage) 组合进行累加
 *
 * 实现流程：
 * 1. 预缓存所有队列中有效物品的本地化信息
 * 2. 分别统计三个队列中有效物品的数量
 * 3. 合并统计结果生成最终视图
 */
fun CpuCoreStatus.convertToTaskQueues(getLocalItem: (Item) -> LocalizedItem?): List<CpuTaskQueue> {
    // region 预缓存阶段：收集所有队列中的有效物品本地化数据
    // 通过序列处理合并三个队列，保证处理效率（避免创建多个临时集合）
    val sampleCache = sequenceOf(activeItems, storedItems, pendingItems)
        .flatten()
        .mapNotNull { item ->
            // 获取本地化信息，同时执行数据一致性校验
            getLocalItem(item)?.also { localizedItem ->
                // 防御性编程：确保本地化后的物品元数据与原始物品一致
                require(localizedItem.item.name == item.name && localizedItem.item.damage == item.damage) {
                    "物品元数据不一致！原始物品: (${item.name}, ${item.damage})，" +
                            "本地化物品: (${localizedItem.item.name}, ${localizedItem.item.damage})"
                }
            }
        }
        // 以 (name, damage) 为唯一键建立缓存，供后续快速查找
        .associateBy { it.item.name to it.item.damage }
    // endregion

    // region 统计阶段：过滤并聚合各队列物品数量

    /**
     * 聚合有效物品数量（基于size属性累加）
     *
     * @param items 待处理物品队列
     * @return 映射表结构：Key为(name, damage)，Value为对应物品的size总和
     */
    fun filterAndAggregate(items: List<Item>) = items
        .mapNotNull { item -> getLocalItem(item)?.item }
        .groupingBy { it.name to it.damage }
        .fold(0) { acc, item -> acc + item.size }
    // 并行处理三个队列的统计工作
    val activeSums = filterAndAggregate(activeItems)   // 活动队列统计
    val storedSums = filterAndAggregate(storedItems)   // 存储队列统计
    val pendingSums = filterAndAggregate(pendingItems) // 待处理队列统计
    // endregion

    // region 结果合成阶段：合并统计结果生成最终视图
    // 收集所有有效物品的唯一标识键（已通过预缓存确保存在）
    val allKeys = sequenceOf(activeSums, storedSums, pendingSums)
        .flatMap { it.keys }
        .toSet()

    return allKeys
        .map { key ->
            // 从预缓存中安全获取本地化数据（!! 断言安全原因：key来源自已过滤的统计结果）
            val sample = sampleCache[key]!!
            CpuTaskQueue(
                itemName = sample.chineseName,    // 本地化名称
                activeNumber = activeSums[key] ?: 0,  // 活动队列数量（无记录时补零）
                pendingNumber = pendingSums[key] ?: 0,// 待处理队列数量
                storedNumber = storedSums[key] ?: 0,  // 存储队列数量
                imagePath = sample.imgPath        // 本地化图标路径
            )
        }
        // 按中文名称字典序排列结果
        .sortedBy { it.itemName }
    // endregion
}


    /**
 * 将 CPU 详情转换为图片
 *
 * @param itemUtil 本地化信息获取工具
 * @param cpuNumber CPU 编号 (如果CPU名称为空有显示编号)
 */
fun CpuDetail.toImage(itemUtil: ItemUtil, cpuNumber: Int): BufferedImage {
    val taskQueues = cpu.convertToTaskQueues { itemUtil.getLocalItem(it) }

    // 尺寸参数调整
    val columnCount = 3
    val cardWidth = 200
    val cardHeight = 100
    val horizontalPadding = 20
    val verticalPadding = 15
    val headerHeight = 50  // 顶部标题区域高度

    // 计算画布尺寸（增加顶部空间）
    val canvasWidth = (cardWidth + horizontalPadding) * columnCount + horizontalPadding
    val rowCount = ceil(taskQueues.size.toDouble() / columnCount).toInt()
    val canvasHeight = headerHeight + (cardHeight + verticalPadding) * rowCount + verticalPadding

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
    g2d.color = Color(0xDBDBDB)
    g2d.fillRect(0, 0, canvasWidth, canvasHeight)

    // 先绘制CPU标题信息
    drawCpuHeader(this, cpuNumber, g2d, canvasWidth, headerHeight)

    // 绘制任务卡片（下移headerHeight）
    taskQueues.forEachIndexed { index, queue ->
        val col = index % columnCount
        val row = index / columnCount
        val x = horizontalPadding + col * (cardWidth + horizontalPadding)
        val y = headerHeight + verticalPadding + row * (cardHeight + verticalPadding)
        drawTaskCard(g2d, queue, x, y, cardWidth, cardHeight)
    }

    // 绘制边框
    drawBorderEffect(g2d, canvasWidth, canvasHeight)

    g2d.dispose()
    return image
}

/**
 * 绘制顶部标题样式的CPU信息
 */
private fun drawCpuHeader(cpuDetail: CpuDetail, cpuNumber: Int, g: Graphics2D, width: Int, height: Int) {
    // 背景条
    g.color = Color(0xC6C6C6)
    g.fillRect(0, 0, width, height)

    // 文字内容
    val cpuName = cpuDetail.name.ifEmpty { "CPU #$cpuNumber" }
    val statusText = "状态: ${if (cpuDetail.busy) "忙碌中" else "空闲"}"
    val storageText = "存储: ${cpuDetail.storage / 1024} KB"
    val coresText = "处理器: ${cpuDetail.coprocessors} 核"

    val fullText = listOf(cpuName, statusText, storageText, coresText).joinToString("  |  ")

    // 文字样式
    g.color = Color(0x595959)
    g.font = Font("Microsoft YaHei", Font.BOLD, 16)

    // 居中计算
    val metrics = g.fontMetrics
    val textWidth = metrics.stringWidth(fullText)
    val startX = (width - textWidth) / 2
    val startY = (height - metrics.height) / 2 + metrics.ascent

    g.drawString(fullText, startX.coerceAtLeast(20), startY)  // 保证最小左边距

    // 底部装饰线
    g.color = Color(0x6a6a6a)
    g.drawLine(0, height - 2, width, height - 2)
    g.color = Color(0x999999)
    g.drawLine(0, height - 1, width, height - 1)
}

private fun drawTaskCard(g: Graphics2D, queue: CpuTaskQueue, x: Int, y: Int, width: Int, height: Int) {
    // 背景色
    val bgColor = when {
        queue.activeNumber > 0 && queue.pendingNumber > 0 -> Color(0xA6C699)
        queue.pendingNumber > 0 -> Color(0xE8E5CA)
        queue.storedNumber > 0 -> Color(0xDBDBDB)
        else -> Color.WHITE
    }

    // 绘制卡片背景
    g.color = bgColor
    g.fillRoundRect(x, y, width, height, 8, 8)

    // 绘制边框
    g.color = Color(0x8B8B8B)
    g.drawRoundRect(x, y, width, height, 8, 8)

    // 图标处理
    try {
        val icon = ImageIO.read(File(queue.imagePath)).getScaledInstance(48, 48, Image.SCALE_SMOOTH)
        g.drawImage(icon, x + width - 60, y + (height - 48) / 2, null)
    } catch (e: Exception) {
        // 图标加载失败处理
    }

    // 文字排版
    val metrics = g.fontMetrics
    val textX = x + 15
    var textY = y + 30

    // 物品名称（带截断）
    g.color = Color.BLACK
    g.font = Font("Microsoft YaHei", Font.BOLD, 14)
    val itemName = truncateText(queue.itemName, metrics, width - 90)
    g.drawString(itemName, textX, textY)

    // 数值信息
    textY += 25
    g.font = Font("Microsoft YaHei", Font.PLAIN, 12)
    listOf(
        "正在合成" to queue.activeNumber,
        "计划合成" to queue.pendingNumber,
        "现存数量" to queue.storedNumber
    ).forEach { (label, value) ->
        if (value > 0) {
            g.drawString("$label: $value", textX, textY)
            textY += 20
        }
    }
}

private fun drawBorderEffect(g: Graphics2D, width: Int, height: Int) {
    // 外框
    g.color = Color(0x373737)
    g.drawRect(0, 0, width - 1, height - 1)

    // 内高光
    g.color = Color.WHITE
    g.drawLine(1, 1, width - 2, 1)
    g.drawLine(1, 1, 1, height - 2)

    // 阴影
    g.color = Color(0x666666)
    g.drawLine(width - 1, 0, width - 1, height - 1)
    g.drawLine(0, height - 1, width - 1, height - 1)
}

private fun truncateText(text: String, metrics: FontMetrics, maxWidth: Int): String {
    if (metrics.stringWidth(text) <= maxWidth) return text

    val ellipsis = "..."
    var truncated = text
    while (metrics.stringWidth(truncated + ellipsis) > maxWidth && truncated.isNotEmpty()) {
        truncated = truncated.dropLast(1)
    }
    return truncated + ellipsis
}

/**
 * 合并图片
 *
 */
fun List<BufferedImage>.merge(): BufferedImage {
    // 计算合并图片尺寸
    val maxWidth = maxOf { it.width }
    val totalHeight = sumOf { it.height }

    // 创建合并后的图片对象
    val combinedImage = BufferedImage(
        maxWidth,
        totalHeight,
        BufferedImage.TYPE_INT_ARGB
    ).apply {
        // 初始化白色背景
        createGraphics().apply {
            color = Color.WHITE
            fillRect(0, 0, maxWidth, totalHeight)
            dispose()
        }
    }

    // 绘制所有图片到合并画布
    with(combinedImage.createGraphics()) {
        var yOffset = 0
        forEach { img ->
            drawImage(img, 0, yOffset, null)
            yOffset += img.height
        }
        dispose()
    }

    return combinedImage
}

/**
 * 批量转换 CPU 详情为图片，并合并为一张大图
 *
 * @param itemUtil 本地化信息获取工具
 * @return 合并后的图片
 */
fun List<CpuDetail>.toImage(itemUtil: ItemUtil): BufferedImage {
    val images = mutableListOf<BufferedImage>()

    withIndex().forEach { (index, cpuDetail) ->
        val image = cpuDetail.toImage(itemUtil, index + 1)
        images.add(image)
    }

    return images.merge()
}

/**
 * 将 BufferedImage 转换为 InputStream
 *
 * @param format 图片格式
 * @return 图片流
 */
fun BufferedImage.toInputStream(format: String = "PNG"): InputStream {
    val outputStream = ByteArrayOutputStream().apply {
        use {
            if (!ImageIO.write(this@toInputStream, format, this)) {
                throw IllegalArgumentException("不支持的图片格式: $format")
            }
        }
    }
    return ByteArrayInputStream(outputStream.toByteArray())
}


