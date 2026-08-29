package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CornerPoint
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.setValue
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation

@OptIn(ExperimentalForeignApi::class)
class IosPerspectiveCropper(
    private val dispatchers: AppDispatchers
) : PerspectiveCropper {
    override suspend fun upright(frame: CameraFrame): Outcome<CameraFrame> {
        return FailureCatcher.ocr("crop.upright") {
            withContext(dispatchers.computation) {
                uprightBlocking(frame)
            }
        }
    }

    override suspend fun crop(uprightFrame: CameraFrame, quad: SelectionQuad): Outcome<CameraFrame> {
        return FailureCatcher.ocr("crop.warp") {
            withContext(dispatchers.computation) {
                cropBlocking(uprightFrame, quad)
            }
        }
    }

    private fun uprightBlocking(frame: CameraFrame): CameraFrame {
        val image: UIImage = UIImage.imageWithData(frame.bytes.toNSData())
            ?: throw IllegalStateException("Could not decode the captured JPEG")
        val width: Double = image.size.useContents { width }
        val height: Double = image.size.useContents { height }
        if (image.imageOrientation == UIImageOrientation.UIImageOrientationUp) {
            return frame.copy(width = width.toInt(), height = height.toInt(), rotationDegrees = 0)
        }
        UIGraphicsBeginImageContextWithOptions(image.size, false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        val redrawn: UIImage? = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        val normalized: UIImage = redrawn
            ?: throw IllegalStateException("Could not normalize the photo orientation")
        return encodeJpeg(normalized)
    }

    private fun cropBlocking(uprightFrame: CameraFrame, quad: SelectionQuad): CameraFrame {
        val source: CIImage = CIImage.imageWithData(uprightFrame.bytes.toNSData())
            ?: throw IllegalStateException("Could not decode the upright JPEG")
        val width: Double = source.extent.useContents { size.width }
        val height: Double = source.extent.useContents { size.height }
        val filter: CIFilter = CIFilter.filterWithName("CIPerspectiveCorrection")
            ?: throw IllegalStateException("CIPerspectiveCorrection is unavailable")
        filter.setValue(source, forKey = "inputImage")
        filter.setValue(ciPoint(quad.topLeft, width, height), forKey = "inputTopLeft")
        filter.setValue(ciPoint(quad.topRight, width, height), forKey = "inputTopRight")
        filter.setValue(ciPoint(quad.bottomRight, width, height), forKey = "inputBottomRight")
        filter.setValue(ciPoint(quad.bottomLeft, width, height), forKey = "inputBottomLeft")
        val output: CIImage = filter.outputImage
            ?: throw IllegalStateException("Perspective correction produced no image")
        return renderJpeg(output)
    }

    /** Core Image uses a bottom-left origin; the quad uses top-left. */
    private fun ciPoint(point: CornerPoint, width: Double, height: Double): CIVector {
        val x: Double = point.x.toDouble() * width
        val y: Double = height - point.y.toDouble() * height
        return CIVector.vectorWithX(x, Y = y)
    }

    private fun renderJpeg(output: CIImage): CameraFrame {
        val context = CIContext()
        val cgImage = context.createCGImage(output, fromRect = output.extent)
            ?: throw IllegalStateException("Could not render the cropped image")
        val image = UIImage.imageWithCGImage(cgImage)
        val frame: CameraFrame = encodeJpeg(image)
        CGImageRelease(cgImage)
        return frame
    }

    private fun encodeJpeg(image: UIImage): CameraFrame {
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

    private companion object {
        const val JPEG_QUALITY: Double = 0.9
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) {
        return NSData()
    }
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) ?: NSData()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val pointer = bytes ?: return ByteArray(0)
    return pointer.readBytes(length.toInt())
}
