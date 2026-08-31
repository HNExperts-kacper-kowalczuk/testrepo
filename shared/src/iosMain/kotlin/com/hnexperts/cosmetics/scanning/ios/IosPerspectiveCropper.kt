package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CornerPoint
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGImageRelease
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.CIImage
import platform.CoreImage.CIVector
import platform.UIKit.UIImage
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
        return IosStillImages.encodeJpeg(IosStillImages.uprightPixels(image))
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
        val frame: CameraFrame = IosStillImages.encodeJpeg(image)
        CGImageRelease(cgImage)
        return frame
    }
}
