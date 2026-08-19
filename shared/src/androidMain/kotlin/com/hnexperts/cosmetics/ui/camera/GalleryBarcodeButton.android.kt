package com.hnexperts.cosmetics.ui.camera

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery
import com.hnexperts.cosmetics.scanning.android.AndroidBarcodeMapper
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.resume

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            onEmpty()
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
    TextButton(
        onClick = { picker.launch("image/*") },
        enabled = enabled,
        modifier = modifier
    ) {
        Text(stringResource(Res.string.camera_gallery))
    }
}

private suspend fun decodeGalleryBarcode(context: android.content.Context, uri: Uri): BarcodePayload? {
    val bitmap = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
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
        scanner.process(image)
            .addOnSuccessListener { codes ->
                val payload: BarcodePayload? = codes.firstNotNullOfOrNull(AndroidBarcodeMapper::toPayload)
                continuation.resume(payload)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}
