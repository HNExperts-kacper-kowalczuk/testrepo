package com.hnexperts.cosmetics.scanning.domain

enum class ScannerMode {
    BARCODE,
    INGREDIENT_LIST
}

enum class CameraPermissionStatus {
    UNKNOWN,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED,
    UNSUPPORTED
}

data class CameraFrame(
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int
)
