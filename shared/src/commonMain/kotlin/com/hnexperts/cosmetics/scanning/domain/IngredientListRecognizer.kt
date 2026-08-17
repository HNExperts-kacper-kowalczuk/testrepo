package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.failure.Outcome

interface IngredientListRecognizer {
    suspend fun recognize(frame: CameraFrame): Outcome<OcrDocument>
}
