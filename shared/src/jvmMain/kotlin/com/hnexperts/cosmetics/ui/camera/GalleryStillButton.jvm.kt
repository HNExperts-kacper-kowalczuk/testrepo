package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery_inci
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun GalleryStillButton(
    enabled: Boolean,
    onFrame: (CameraFrame) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    GalleryBarcodeIconButton(
        enabled = enabled,
        onClick = onCancel,
        modifier = modifier,
        contentDescription = stringResource(Res.string.camera_gallery_inci)
    )
}
