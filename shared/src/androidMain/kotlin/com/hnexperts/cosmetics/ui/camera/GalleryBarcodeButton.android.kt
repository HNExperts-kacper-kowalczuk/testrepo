package com.hnexperts.cosmetics.ui.camera

import android.content.Context
import android.graphics.Bitmap
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.stringResource
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
    TextButton(
        onClick = { picker.launch("image/*") },
        enabled = enabled,
        modifier = modifier
    ) {
        Text(stringResource(Res.string.camera_gallery))
    }
}

private suspend fun decodeGalleryBarcode(context: Context, uri: Uri): BarcodePayload? {
    val bitmap: Bitmap = try {
        softwareArgb8888(decodeGalleryBitmap(context, uri))
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

private fun decodeGalleryBitmap(context: Context, uri: Uri): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    }
    @Suppress("DEPRECATION")
    return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
}

private fun softwareArgb8888(bitmap: Bitmap): Bitmap {
    if (bitmap.config != Bitmap.Config.HARDWARE && bitmap.config == Bitmap.Config.ARGB_8888) {
        return bitmap
    }
    val copy: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return bitmap
    if (bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.recycle()
    }
    return copy
}
