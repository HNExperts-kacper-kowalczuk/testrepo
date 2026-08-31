package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery_inci
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.ios.IosStillImages
import com.hnexperts.cosmetics.scanning.ios.presentIosGalleryPicker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import platform.UIKit.UIImage

@Composable
actual fun GalleryStillButton(
    enabled: Boolean,
    onFrame: (CameraFrame) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    GalleryBarcodeIconButton(
        enabled = enabled,
        onClick = {
            presentIosGalleryPicker(
                onImage = { image: UIImage ->
                    scope.launch {
                        val frame: CameraFrame? = encodeGalleryStill(image)
                        if (frame == null) {
                            onEmpty()
                        } else {
                            onFrame(frame)
                        }
                    }
                },
                onEmpty = onEmpty,
                onCancel = onCancel
            )
        },
        modifier = modifier,
        contentDescription = stringResource(Res.string.camera_gallery_inci)
    )
}

private suspend fun encodeGalleryStill(image: UIImage): CameraFrame? {
    return try {
        withContext(Dispatchers.Default) {
            IosStillImages.fromGallery(image)
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        null
    }
}
