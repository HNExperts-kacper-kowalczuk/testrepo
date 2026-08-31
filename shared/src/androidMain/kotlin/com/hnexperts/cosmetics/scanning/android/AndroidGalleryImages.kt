package com.hnexperts.cosmetics.scanning.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.InputStream

/**
 * Decodes a gallery URI into a software ARGB bitmap.
 * API 28+ ImageDecoder already applies EXIF; older APIs need [decodeUprightArgb8888].
 */
internal object AndroidGalleryImages {
    fun decodeArgb8888(context: Context, uri: Uri): Bitmap {
        return softwareArgb8888(decodeBitmap(context, uri))
    }

    fun decodeUprightArgb8888(context: Context, uri: Uri): Bitmap {
        val bitmap: Bitmap = decodeArgb8888(context, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return bitmap
        }
        return applyExifRotation(context, uri, bitmap)
    }

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        }
        @Suppress("DEPRECATION")
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    private fun softwareArgb8888(bitmap: Bitmap): Bitmap {
        if (bitmap.config != Bitmap.Config.HARDWARE && bitmap.config == Bitmap.Config.ARGB_8888) {
            return bitmap
        }
        val copy: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return bitmap
        if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.recycle()
        }
        return copy
    }

    private fun applyExifRotation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val degrees: Int = exifRotationDegrees(context, uri)
        if (degrees == 0) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun exifRotationDegrees(context: Context, uri: Uri): Int {
        val stream: InputStream = context.contentResolver.openInputStream(uri) ?: return 0
        val orientation: Int = stream.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
