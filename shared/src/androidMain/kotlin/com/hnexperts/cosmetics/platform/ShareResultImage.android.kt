package com.hnexperts.cosmetics.platform

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.hnexperts.cosmetics.di.AndroidAppContext
import com.hnexperts.cosmetics.evaluation.application.ShareResultImageLayout
import java.io.ByteArrayOutputStream
import java.io.File

actual fun encodeSharePng(layout: ShareResultImageLayout): ByteArray {
    val bitmap: Bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = FONT_SIZE
        typeface = Typeface.SANS_SERIF
    }
    var y: Float = TOP
    for (line in layout.drawLines()) {
        canvas.drawText(line, LEFT, y, paint)
        y += LINE_HEIGHT
    }
    val out: ByteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    bitmap.recycle()
    return out.toByteArray()
}

actual fun sharePngBytes(title: String, png: ByteArray) {
    val context = AndroidAppContext.activity() ?: AndroidAppContext.current() ?: return
    val file: File = writeShareFile(File(context.cacheDir, "share"), png) ?: return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(share, title).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

private fun writeShareFile(directory: File, png: ByteArray): File? {
    if (!directory.exists() && !directory.mkdirs()) {
        return null
    }
    val file = File(directory, "result.png")
    file.writeBytes(png)
    return file
}

private const val WIDTH: Int = 1080
private const val HEIGHT: Int = 1350
private const val LEFT: Float = 48f
private const val TOP: Float = 80f
private const val FONT_SIZE: Float = 36f
private const val LINE_HEIGHT: Float = 52f
