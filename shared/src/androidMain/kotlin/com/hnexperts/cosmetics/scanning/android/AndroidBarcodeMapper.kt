package com.hnexperts.cosmetics.scanning.android

import com.google.mlkit.vision.barcode.common.Barcode
import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.scanning.domain.BarcodeFormat
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload

object AndroidBarcodeMapper {
    fun toPayload(barcode: Barcode): BarcodePayload? {
        val raw: String = barcode.rawValue ?: return null
        val format: BarcodeFormat = formatOf(barcode.format) ?: return null
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            return null
        }
        return BarcodePayload(gtin = gtin, format = format)
    }

    private fun formatOf(mlKitFormat: Int): BarcodeFormat? {
        return when (mlKitFormat) {
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
            else -> null
        }
    }
}
