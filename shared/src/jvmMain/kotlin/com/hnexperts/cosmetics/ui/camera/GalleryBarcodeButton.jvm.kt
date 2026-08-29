package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    GalleryBarcodeIconButton(enabled = enabled, onClick = onCancel, modifier = modifier)
}
