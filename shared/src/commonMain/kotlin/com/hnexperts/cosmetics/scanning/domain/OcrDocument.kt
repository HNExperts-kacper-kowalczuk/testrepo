package com.hnexperts.cosmetics.scanning.domain

data class OcrBlock(
    val text: String,
    val confidence: Float
)

data class OcrDocument(
    val rawText: String,
    val blocks: List<OcrBlock>,
    val averageConfidence: Float
)
