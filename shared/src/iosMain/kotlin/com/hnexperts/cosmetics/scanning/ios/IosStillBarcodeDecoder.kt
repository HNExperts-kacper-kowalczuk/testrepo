package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer
import com.hnexperts.cosmetics.scanning.domain.BarcodeFormat
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNBarcodeSymbologyEAN13
import platform.Vision.VNBarcodeSymbologyEAN8
import platform.Vision.VNBarcodeSymbologyUPCE
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler

@OptIn(ExperimentalForeignApi::class)
object IosStillBarcodeDecoder {
    fun decode(image: UIImage): BarcodePayload? {
        val jpeg = UIImageJPEGRepresentation(image, 0.9) ?: return null
        var payload: BarcodePayload? = null
        val request = VNDetectBarcodesRequest { request, _ ->
            val observations = request?.results.orEmpty()
            for (item in observations) {
                val observation = item as? VNBarcodeObservation ?: continue
                payload = toPayload(observation)
                if (payload != null) {
                    break
                }
            }
        }
        request.symbologies = listOf(VNBarcodeSymbologyEAN13, VNBarcodeSymbologyEAN8, VNBarcodeSymbologyUPCE)
        val handler = VNImageRequestHandler(data = jpeg, options = emptyMap<Any?, Any>())
        handler.performRequests(listOf(request), error = null)
        return payload
    }

    private fun toPayload(observation: VNBarcodeObservation): BarcodePayload? {
        val raw: String = observation.payloadStringValue ?: return null
        val gtin: String = GtinNormalizer.normalize(raw)
        if (gtin.length < 8) {
            return null
        }
        val format: BarcodeFormat = formatOf(observation.symbology) ?: BarcodeFormat.EAN_13
        return BarcodePayload(gtin = gtin, format = format)
    }

    private fun formatOf(symbology: String?): BarcodeFormat? {
        return when (symbology) {
            VNBarcodeSymbologyEAN13 -> BarcodeFormat.EAN_13
            VNBarcodeSymbologyEAN8 -> BarcodeFormat.EAN_8
            VNBarcodeSymbologyUPCE -> BarcodeFormat.UPC_E
            else -> null
        }
    }
}
