package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.scanning.domain.BarcodeFormat
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeUPCECode

object IosBarcodeMapper {
    fun toPayload(code: AVMetadataMachineReadableCodeObject): BarcodePayload? {
        val raw: String = code.stringValue ?: return null
        val format: BarcodeFormat = formatOf(code.type) ?: return null
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            return null
        }
        return BarcodePayload(gtin = gtin, format = format)
    }

    private fun formatOf(type: String?): BarcodeFormat? {
        return when (type) {
            AVMetadataObjectTypeEAN13Code -> BarcodeFormat.EAN_13
            AVMetadataObjectTypeEAN8Code -> BarcodeFormat.EAN_8
            AVMetadataObjectTypeUPCECode -> BarcodeFormat.UPC_E
            else -> null
        }
    }
}
