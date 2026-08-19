package com.hnexperts.cosmetics.scanning.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import java.io.ByteArrayOutputStream
import kotlin.math.max

internal object AndroidStillImages {
    const val OCR_MAX_EDGE: Int = 1280
    const val CROP_MAX_EDGE: Int = 2048
    private const val JPEG_QUALITY: Int = 90

    fun decodeUpright(frame: CameraFrame, maxEdge: Int): Bitmap {
        val decoded: Bitmap = BitmapFactory.decodeByteArray(frame.bytes, 0, frame.bytes.size)
            ?: throw IllegalStateException("Could not decode the captured JPEG")
        val rotated: Bitmap = rotate(decoded, frame.rotationDegrees)
        return constrain(rotated, maxEdge)
    }

    fun encode(bitmap: Bitmap): CameraFrame {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        return CameraFrame(
            bytes = output.toByteArray(),
            width = bitmap.width,
            height = bitmap.height,
            rotationDegrees = 0
        )
    }

    private fun rotate(source: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) {
            return source
        }
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun constrain(source: Bitmap, maxEdge: Int): Bitmap {
        val longest: Int = max(source.width, source.height)
        if (longest <= maxEdge) {
            return source
        }
        val scale: Float = maxEdge.toFloat() / longest.toFloat()
        val width: Int = (source.width * scale).toInt().coerceAtLeast(1)
        val height: Int = (source.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}
