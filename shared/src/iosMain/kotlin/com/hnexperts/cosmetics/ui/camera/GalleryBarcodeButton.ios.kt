package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.ios.IosStillBarcodeDecoder
import com.hnexperts.cosmetics.scanning.ios.presentIosGalleryPicker
import platform.UIKit.UIImage

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    GalleryBarcodeIconButton(
        enabled = enabled,
        onClick = {
            presentIosGalleryPicker(
                onImage = { image: UIImage ->
                    val payload: BarcodePayload? = IosStillBarcodeDecoder.decode(image)
                    if (payload == null) {
                        onEmpty()
                    } else {
                        onBarcode(payload)
                    }
                },
                onEmpty = onEmpty,
                onCancel = onCancel
            )
        },
        modifier = modifier
    )
}
