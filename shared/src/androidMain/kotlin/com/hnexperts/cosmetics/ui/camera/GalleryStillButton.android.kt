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
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery_inci
import com.hnexperts.cosmetics.scanning.android.AndroidGalleryImages
import com.hnexperts.cosmetics.scanning.android.AndroidStillImages
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun GalleryStillButton(
    enabled: Boolean,
    onFrame: (CameraFrame) -> Unit,
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
            val frame: CameraFrame? = withContext(Dispatchers.Default) {
                decodeGalleryStill(context, uri)
            }
            if (frame == null) {
                onEmpty()
            } else {
                onFrame(frame)
            }
        }
    }
    GalleryBarcodeIconButton(
        enabled = enabled,
        onClick = { picker.launch("image/*") },
        modifier = modifier,
        contentDescription = stringResource(Res.string.camera_gallery_inci)
    )
}

private fun decodeGalleryStill(context: Context, uri: Uri): CameraFrame? {
    return try {
        val bitmap: Bitmap = AndroidGalleryImages.decodeUprightArgb8888(context, uri)
        AndroidStillImages.fromGalleryBitmap(bitmap)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
}
