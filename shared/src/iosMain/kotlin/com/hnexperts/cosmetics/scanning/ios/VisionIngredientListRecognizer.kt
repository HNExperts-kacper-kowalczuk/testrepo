package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.OcrBlock
import com.hnexperts.cosmetics.scanning.domain.OcrDocument
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate

@OptIn(ExperimentalForeignApi::class)
class VisionIngredientListRecognizer(
    private val dispatchers: AppDispatchers
) : IngredientListRecognizer {
    override suspend fun recognize(frame: CameraFrame): Outcome<OcrDocument> {
        return FailureCatcher.ocr("ocr.recognize") {
            withContext(dispatchers.computation) {
                recognizeBlocking(frame)
            }
        }
    }

    private fun recognizeBlocking(frame: CameraFrame): OcrDocument {
        val jpeg: NSData = frame.bytes.toNSData()
        val lines: MutableList<String> = mutableListOf()
        val request = VNRecognizeTextRequest { request, error ->
            if (error != null) {
                throw IllegalStateException(error.localizedDescription)
            }
            val observations = request?.results.orEmpty()
            for (item in observations) {
                val observation = item as? VNRecognizedTextObservation ?: continue
                val best: VNRecognizedText = observation.topCandidates(1u).firstOrNull() as? VNRecognizedText ?: continue
                lines.add(best.string)
            }
        }
        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
        val handler = VNImageRequestHandler(data = jpeg, options = emptyMap<Any?, Any>())
        val ok: Boolean = handler.performRequests(listOf(request), error = null)
        if (!ok && lines.isEmpty()) {
            throw IllegalStateException("Vision text recognition failed")
        }
        val joined: String = lines.joinToString(", ")
        return OcrDocument(
            rawText = joined,
            blocks = lines.map { line -> OcrBlock(text = line, confidence = 1f) },
            averageConfidence = 1f
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) {
        return NSData()
    }
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
            ?: NSData()
    }
}
