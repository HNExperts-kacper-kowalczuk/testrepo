package com.hnexperts.cosmetics.platform

import com.hnexperts.cosmetics.evaluation.application.ShareResultImageLayout
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.posix.memcpy
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalForeignApi::class)
actual fun encodeSharePng(layout: ShareResultImageLayout): ByteArray {
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(WIDTH, HEIGHT))
    val image: UIImage = renderer.imageWithActions { _ ->
        UIColor.whiteColor.setFill()
        UIRectFill(CGRectMake(0.0, 0.0, WIDTH, HEIGHT))
        val attrs: Map<Any?, Any?> = mapOf(
            NSFontAttributeName to UIFont.systemFontOfSize(FONT_SIZE),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        var y: Double = TOP
        for (line in layout.drawLines()) {
            val nsLine: NSString = NSString.create(string = line)
            nsLine.drawAtPoint(CGPointMake(LEFT, y), withAttributes = attrs)
            y += LINE_HEIGHT
        }
    }
    val data = UIImagePNGRepresentation(image) ?: return ByteArray(0)
    return ByteArray(data.length.toInt()).also { bytes ->
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), data.bytes, data.length)
        }
    }
}

actual fun sharePngBytes(title: String, png: ByteArray) {
    if (png.isEmpty()) {
        return
    }
    val root = iosRootViewController() ?: return
    val image: UIImage = png.toUiImage() ?: return
    val controller = UIActivityViewController(
        activityItems = listOf(image),
        applicationActivities = null
    )
    controller.popoverPresentationController?.sourceView = root.view
    root.presentViewController(controller, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toUiImage(): UIImage? {
    if (isEmpty()) {
        return null
    }
    return usePinned { pinned ->
        val data = platform.Foundation.NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        UIImage.imageWithData(data)
    }
}

private const val WIDTH: Double = 1080.0
private const val HEIGHT: Double = 1350.0
private const val LEFT: Double = 48.0
private const val TOP: Double = 48.0
private const val FONT_SIZE: Double = 36.0
private const val LINE_HEIGHT: Double = 52.0
