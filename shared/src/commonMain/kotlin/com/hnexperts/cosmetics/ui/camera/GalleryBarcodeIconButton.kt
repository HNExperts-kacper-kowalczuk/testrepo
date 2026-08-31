package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery
import com.hnexperts.cosmetics.ui.chrome.AppActionIcons
import com.hnexperts.cosmetics.ui.chrome.AppIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun GalleryBarcodeIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppIconButton(
        imageVector = AppActionIcons.Gallery,
        contentDescription = stringResource(Res.string.camera_gallery),
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
}
