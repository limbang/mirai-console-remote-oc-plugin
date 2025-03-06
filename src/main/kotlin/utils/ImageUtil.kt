/*
 * Copyright (c) 2025. limbang and contributors.
 *
 * 此源代码的使用受 GNU AGPLv3 许可证的约束，该许可证可在"LICENSE"文件中找到。
 * Use of this source code is governed by the GNU AGPLv3 license that can be found in the "LICENSE" file.
 */

package top.limbang.remoteoc.utils


import top.limbang.remoteoc.RemoteOC
import java.awt.AlphaComposite
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * 绘制 Minecraft 风格的边框效果
 *
 * @param g 画笔
 * @param width 宽度
 * @param height 高度
 *
 * @see MinecraftStyle
 */
fun drawBorderEffect(g: Graphics2D, width: Int, height: Int) = with(MinecraftStyle) {
    // 预先计算常用倍数
    val twoB = BORDER_WIDTH * 2
    val threeB = BORDER_WIDTH * 3
    val fourB = BORDER_WIDTH * 4
    val innerWidth = width - BORDER_WIDTH * 5
    val innerHeight = height - BORDER_WIDTH * 5

    // 定义一个局部数据类用于描述矩形区域
    data class Quad(val x: Int, val y: Int, val w: Int, val h: Int)

    // 简化绘制函数
    fun fill(quad: Quad) = g.fillRect(quad.x, quad.y, quad.w, quad.h)

    // 1. 清空透明区域
    g.composite = AlphaComposite.Clear
    listOf(
        // 左上角
        Quad(0, 0, twoB, BORDER_WIDTH),
        Quad(0, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH),
        // 右下角
        Quad(width - BORDER_WIDTH, height - twoB, BORDER_WIDTH, BORDER_WIDTH),
        Quad(width - twoB, height - BORDER_WIDTH, twoB, BORDER_WIDTH),
        // 右上角
        Quad(width - threeB, 0, threeB, BORDER_WIDTH),
        Quad(width - twoB, BORDER_WIDTH, twoB, BORDER_WIDTH),
        Quad(width - BORDER_WIDTH, twoB, BORDER_WIDTH, BORDER_WIDTH),
        // 左下角
        Quad(0, height - threeB, BORDER_WIDTH, BORDER_WIDTH),
        Quad(0, height - twoB, twoB, BORDER_WIDTH),
        Quad(0, height - BORDER_WIDTH, threeB, BORDER_WIDTH)
    ).forEach(::fill)

    // 2. 绘制边框
    g.composite = AlphaComposite.SrcOver
    g.color = BORDER_COLOR
    listOf(
        // 横向边框
        Quad(twoB, 0, innerWidth, BORDER_WIDTH),
        Quad(threeB, height - BORDER_WIDTH, innerWidth, BORDER_WIDTH),
        // 纵向边框
        Quad(0, twoB, BORDER_WIDTH, innerHeight),
        Quad(width - BORDER_WIDTH, threeB, BORDER_WIDTH, innerHeight),
        // 圆角装饰点
        Quad(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH),
        Quad(width - twoB, height - twoB, BORDER_WIDTH, BORDER_WIDTH),
        Quad(width - threeB, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH),
        Quad(width - twoB, twoB, BORDER_WIDTH, BORDER_WIDTH),
        Quad(BORDER_WIDTH, height - threeB, BORDER_WIDTH, BORDER_WIDTH),
        Quad(twoB, height - twoB, BORDER_WIDTH, BORDER_WIDTH)
    ).forEach(::fill)

    // 3. 绘制高光
    g.color = BORDER_HIGHLIGHT_COLOR
    listOf(
        Quad(twoB, BORDER_WIDTH, innerWidth, BORDER_HIGHLIGHT_SHADOW_WIDTH),
        Quad(BORDER_WIDTH, twoB, BORDER_HIGHLIGHT_SHADOW_WIDTH, innerHeight),
        Quad(threeB, threeB, BORDER_WIDTH, BORDER_WIDTH)
    ).forEach(::fill)

    // 4. 绘制阴影
    g.color = BORDER_SHADOW_COLOR
    listOf(
        Quad(width - threeB, threeB, BORDER_HIGHLIGHT_SHADOW_WIDTH, innerHeight),
        Quad(threeB, height - threeB, innerWidth, BORDER_HIGHLIGHT_SHADOW_WIDTH),
        Quad(width - fourB, height - fourB, BORDER_WIDTH, BORDER_WIDTH)
    ).forEach(::fill)
}


/**
 * 绘制会截断的文本
 *
 * @param text 文本
 * @param metrics 字体信息
 * @param maxWidth 最大宽度
 * @return
 */
fun truncateText(text: String, metrics: FontMetrics, maxWidth: Int): String {
    if (metrics.stringWidth(text) <= maxWidth) return text

    val ellipsis = "..."
    var truncated = text
    while (metrics.stringWidth(truncated + ellipsis) > maxWidth && truncated.isNotEmpty()) {
        truncated = truncated.dropLast(1)
    }
    return truncated + ellipsis
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

/**
 * 加载图像资源
 * @param name 图片文件名
 * @return BufferedImage 对象，若加载失败则返回 null
 */
fun getImage(name: String): BufferedImage? {
    val resource = RemoteOC::class.java.classLoader.getResourceAsStream("img/$name")
    return resource?.use { ImageIO.read(it) }
}

/**
 * 合并两个 BufferedImage 对象，并在水平方向上拼接 （背景透明）
 *
 * @param image 右边图像
 * @return 合并后的图像
 */
fun BufferedImage.mergeImagesSideBySide(image: BufferedImage): BufferedImage {
    val newWidth = this.width + image.width
    val newHeight = maxOf(this.height, image.height)

    val mergedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = mergedImage.createGraphics()

    g.drawImage(this, 0, 0, null)  // 绘制左侧图片
    g.drawImage(image, this.width, 0, null)  // 绘制右侧图片，从 this.width 开始

    g.dispose()
    return mergedImage
}

/**
 * 合并两个 BufferedImage 对象，并在垂直方向上拼接 （背景透明）
 *
 * @param image 底部图像
 * @return 合并后的图像
 */
fun BufferedImage.mergeImagesTopToBottom(image: BufferedImage): BufferedImage {
    val newWidth = maxOf(this.width, image.width)  // 取较宽的作为合并后的宽度
    val newHeight = this.height + image.height    // 总高度为两张图片高度之和

    val mergedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = mergedImage.createGraphics()

    g.drawImage(this, 0, 0, null)      // 绘制第一张图片（顶部）
    g.drawImage(image, 0, this.height, null) // 绘制第二张图片（底部）

    g.dispose()
    return mergedImage
}

/**
 * 合并一系列 BufferedImage 对象，并在垂直方向上拼接 （背景透明）
 *
 * @return 合并后的图像
 */
fun List<BufferedImage>.mergeImagesTopToBottom(): BufferedImage {
    if (isEmpty()) throw IllegalArgumentException("Image list cannot be empty")

    val newWidth = this.maxOf { it.width }  // 取最宽的图片宽度
    val newHeight = this.sumOf { it.height }  // 计算总高度

    val mergedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = mergedImage.createGraphics()

    var currentY = 0
    for (image in this) {
        val x = (newWidth - image.width) / 2 // 居中绘制
        g.drawImage(image, x, currentY, null)  // 绘制图片
        currentY += image.height  // 递增 Y 坐标
    }

    g.dispose()
    return mergedImage
}