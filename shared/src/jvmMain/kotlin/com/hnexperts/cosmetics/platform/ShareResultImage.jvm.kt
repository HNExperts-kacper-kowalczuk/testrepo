package com.hnexperts.cosmetics.platform

import com.hnexperts.cosmetics.evaluation.application.ShareResultImageLayout
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual fun encodeSharePng(layout: ShareResultImageLayout): ByteArray {
    val image: BufferedImage = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = image.createGraphics()
    try {
        drawShareCard(graphics, layout)
    } finally {
        graphics.dispose()
    }
    val out: ByteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(image, "png", out)
    return out.toByteArray()
}

actual fun sharePngBytes(title: String, png: ByteArray) {
}

private fun drawShareCard(graphics: Graphics2D, layout: ShareResultImageLayout) {
    graphics.color = Color.WHITE
    graphics.fillRect(0, 0, WIDTH, HEIGHT)
    graphics.color = Color.BLACK
    graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE)
    var y: Int = TOP
    for (line in layout.drawLines()) {
        graphics.drawString(line, LEFT, y)
        y += LINE_HEIGHT
    }
}

private const val WIDTH: Int = 1080
private const val HEIGHT: Int = 1350
private const val LEFT: Int = 48
private const val TOP: Int = 80
private const val FONT_SIZE: Int = 36
private const val LINE_HEIGHT: Int = 52
