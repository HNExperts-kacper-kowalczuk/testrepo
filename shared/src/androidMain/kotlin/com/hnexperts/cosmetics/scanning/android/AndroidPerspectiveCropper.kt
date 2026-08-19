package com.hnexperts.cosmetics.scanning.android

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CornerPoint
import com.hnexperts.cosmetics.scanning.domain.PerspectiveCropper
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import kotlinx.coroutines.withContext
import kotlin.math.hypot
import kotlin.math.max

class AndroidPerspectiveCropper(
    private val dispatchers: AppDispatchers
) : PerspectiveCropper {
    override suspend fun upright(frame: CameraFrame): Outcome<CameraFrame> {
        return FailureCatcher.ocr("crop.upright") {
            withContext(dispatchers.computation) {
                AndroidStillImages.encode(
                    AndroidStillImages.decodeUpright(frame, AndroidStillImages.CROP_MAX_EDGE)
                )
            }
        }
    }

    override suspend fun crop(uprightFrame: CameraFrame, quad: SelectionQuad): Outcome<CameraFrame> {
        return FailureCatcher.ocr("crop.warp") {
            withContext(dispatchers.computation) {
                val source: Bitmap = AndroidStillImages.decodeUpright(uprightFrame, AndroidStillImages.CROP_MAX_EDGE)
                AndroidStillImages.encode(warp(source, quad))
            }
        }
    }

    private fun warp(source: Bitmap, quad: SelectionQuad): Bitmap {
        val corners: FloatArray = pixelCorners(source, quad)
        val outWidth: Int = outputWidth(corners)
        val outHeight: Int = outputHeight(corners)
        val matrix = Matrix()
        val destination: FloatArray = floatArrayOf(
            0f, 0f,
            outWidth.toFloat(), 0f,
            outWidth.toFloat(), outHeight.toFloat(),
            0f, outHeight.toFloat()
        )
        check(matrix.setPolyToPoly(corners, 0, destination, 0, 4)) {
            "The selected corners do not form a mappable quadrilateral"
        }
        val result: Bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(source, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
        return result
    }

    private fun pixelCorners(source: Bitmap, quad: SelectionQuad): FloatArray {
        fun px(point: CornerPoint): Pair<Float, Float> {
            return Pair(point.x * source.width, point.y * source.height)
        }
        val (tlx, tly) = px(quad.topLeft)
        val (trx, tryY) = px(quad.topRight)
        val (brx, bry) = px(quad.bottomRight)
        val (blx, bly) = px(quad.bottomLeft)
        return floatArrayOf(tlx, tly, trx, tryY, brx, bry, blx, bly)
    }

    private fun outputWidth(corners: FloatArray): Int {
        val top: Float = hypot(corners[2] - corners[0], corners[3] - corners[1])
        val bottom: Float = hypot(corners[4] - corners[6], corners[5] - corners[7])
        return max(top, bottom).toInt().coerceAtLeast(MIN_EDGE)
    }

    private fun outputHeight(corners: FloatArray): Int {
        val left: Float = hypot(corners[6] - corners[0], corners[7] - corners[1])
        val right: Float = hypot(corners[4] - corners[2], corners[5] - corners[3])
        return max(left, right).toInt().coerceAtLeast(MIN_EDGE)
    }

    private companion object {
        const val MIN_EDGE: Int = 16
    }
}
