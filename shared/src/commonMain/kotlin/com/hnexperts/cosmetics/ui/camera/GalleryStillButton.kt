package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.scanning.domain.CameraFrame

@Composable
expect fun GalleryStillButton(
    enabled: Boolean,
    onFrame: (CameraFrame) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
)
