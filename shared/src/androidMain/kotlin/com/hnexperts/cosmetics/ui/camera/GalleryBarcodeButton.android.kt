package com.hnexperts.cosmetics.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.hnexperts.cosmetics.scanning.android.AndroidBarcodeMapper
import com.hnexperts.cosmetics.scanning.android.AndroidGalleryImages
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            onCancel()
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val payload: BarcodePayload? = decodeGalleryBarcode(context, uri)
            if (payload == null) {
                onEmpty()
            } else {
                onBarcode(payload)
            }
        }
    }
    GalleryBarcodeIconButton(
        enabled = enabled,
        onClick = { picker.launch("image/*") },
        modifier = modifier
    )
}

private suspend fun decodeGalleryBarcode(context: Context, uri: Uri): BarcodePayload? {
    val bitmap: Bitmap = try {
        AndroidGalleryImages.decodeArgb8888(context, uri)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        return null
    }
    val image: InputImage = InputImage.fromBitmap(bitmap, 0)
    val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
    )
    return suspendCancellableCoroutine { continuation ->
        val task = scanner.process(image)
        continuation.invokeOnCancellation { scanner.close() }
        task.addOnSuccessListener { codes ->
            val payload: BarcodePayload? = codes.firstNotNullOfOrNull(AndroidBarcodeMapper::toPayload)
            continuation.resume(payload)
        }
        task.addOnFailureListener {
            continuation.resume(null)
        }
        task.addOnCompleteListener {
            scanner.close()
        }
    }
}
