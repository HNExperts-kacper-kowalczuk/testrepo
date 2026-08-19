package com.hnexperts.cosmetics.ui.camera

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    modifier: Modifier
) {
    TextButton(onClick = onEmpty, enabled = enabled, modifier = modifier) {
        Text(stringResource(Res.string.camera_gallery))
    }
}
