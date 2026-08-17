package com.hnexperts.cosmetics.scanning.jvm

import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.IngredientListRecognizer
import com.hnexperts.cosmetics.scanning.domain.OcrDocument

class UnsupportedIngredientListRecognizer : IngredientListRecognizer {
    override suspend fun recognize(frame: CameraFrame): Outcome<OcrDocument> {
        return Outcome.Err(
            AppFailure.Ocr(
                operation = "ocr.unsupported",
                detail = "On-device OCR is not available on this platform."
            )
        )
    }
}
