package com.hnexperts.cosmetics.scanning.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.OcrBlock
import com.hnexperts.cosmetics.scanning.domain.OcrDocument
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.max

class MlKitIngredientListRecognizer(
    private val dispatchers: AppDispatchers
) : IngredientListRecognizer {
    private val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(frame: CameraFrame): Outcome<OcrDocument> {
        return FailureCatcher.ocr("ocr.recognize") {
            withContext(dispatchers.computation) {
                val bitmap: Bitmap = decodeFrame(frame)
                val image: InputImage = InputImage.fromBitmap(bitmap, 0)
                val text: Text = client.process(image).await()
                toDocument(text)
            }
        }
    }

    private fun decodeFrame(frame: CameraFrame): Bitmap {
        val decoded: Bitmap = BitmapFactory.decodeByteArray(frame.bytes, 0, frame.bytes.size)
            ?: throw IllegalStateException("Could not decode the captured JPEG")
        val rotated: Bitmap = rotate(decoded, frame.rotationDegrees)
        return constrain(rotated, MAX_EDGE)
    }

    private fun rotate(source: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) {
            return source
        }
        val matrix: Matrix = Matrix()
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

    private fun toDocument(text: Text): OcrDocument {
        val blocks: List<OcrBlock> = text.textBlocks.map { block ->
            OcrBlock(text = block.text, confidence = 1f)
        }
        val joined: String = text.text.replace('\n', ',')
        return OcrDocument(
            rawText = joined,
            blocks = blocks,
            averageConfidence = 1f
        )
    }

    private companion object {
        const val MAX_EDGE: Int = 1280
    }
}
