package com.hnexperts.cosmetics.scanning.android

import android.graphics.Bitmap
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

class MlKitIngredientListRecognizer(
    private val dispatchers: AppDispatchers
) : IngredientListRecognizer {
    private val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognize(frame: CameraFrame): Outcome<OcrDocument> {
        return FailureCatcher.ocr("ocr.recognize") {
            withContext(dispatchers.computation) {
                val bitmap: Bitmap = AndroidStillImages.decodeUpright(frame, AndroidStillImages.OCR_MAX_EDGE)
                val image: InputImage = InputImage.fromBitmap(bitmap, 0)
                val text: Text = client.process(image).await()
                toDocument(text)
            }
        }
    }

    private fun toDocument(text: Text): OcrDocument {
        val blocks: List<OcrBlock> = text.textBlocks.map { block ->
            OcrBlock(text = block.text, confidence = 1f)
        }
        return OcrDocument(
            rawText = text.text,
            blocks = blocks,
            averageConfidence = 1f
        )
    }
}
