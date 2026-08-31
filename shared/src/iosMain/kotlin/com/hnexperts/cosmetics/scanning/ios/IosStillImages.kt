package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import kotlin.math.max
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation

@OptIn(ExperimentalForeignApi::class)
internal object IosStillImages {
    private const val JPEG_QUALITY: Double = 0.9
    private const val CROP_MAX_EDGE: Double = 2048.0

    fun fromGallery(image: UIImage): CameraFrame {
        val upright: UIImage = uprightPixels(image)
        return encodeJpeg(constrain(upright, CROP_MAX_EDGE))
    }

    fun encodeJpeg(image: UIImage): CameraFrame {
        val jpeg: NSData = UIImageJPEGRepresentation(image, JPEG_QUALITY)
            ?: throw IllegalStateException("Could not encode the image as JPEG")
        val width: Double = image.size.useContents { width }
        val height: Double = image.size.useContents { height }
        return CameraFrame(
            bytes = jpeg.toByteArray(),
            width = width.toInt(),
            height = height.toInt(),
            rotationDegrees = 0
        )
    }

    fun uprightPixels(image: UIImage): UIImage {
        if (image.imageOrientation == UIImageOrientation.UIImageOrientationUp) {
            return image
        }
        val width: Double = image.size.useContents { width }
        val height: Double = image.size.useContents { height }
        UIGraphicsBeginImageContextWithOptions(image.size, false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        val redrawn: UIImage? = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return redrawn ?: throw IllegalStateException("Could not normalize the photo orientation")
    }

    private fun constrain(source: UIImage, maxEdge: Double): UIImage {
        val width: Double = source.size.useContents { width }
        val height: Double = source.size.useContents { height }
        val longest: Double = max(width, height)
        if (longest <= maxEdge) {
            return source
        }
        val scale: Double = maxEdge / longest
        val targetWidth: Double = (width * scale).coerceAtLeast(1.0)
        val targetHeight: Double = (height * scale).coerceAtLeast(1.0)
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
        source.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        val scaled: UIImage? = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return scaled ?: throw IllegalStateException("Could not scale the gallery photo")
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) {
        return NSData()
    }
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) ?: NSData()
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val pointer = bytes ?: return ByteArray(0)
    return pointer.readBytes(length.toInt())
}
