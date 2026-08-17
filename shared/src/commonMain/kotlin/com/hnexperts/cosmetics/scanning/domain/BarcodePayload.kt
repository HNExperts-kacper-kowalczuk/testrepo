package com.hnexperts.cosmetics.scanning.domain

enum class BarcodeFormat {
    EAN_13,
    EAN_8,
    UPC_A,
    UPC_E,
    UNKNOWN
}

data class BarcodePayload(
    val gtin: String,
    val format: BarcodeFormat
)
