package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.ScannerMode

@Composable
@Suppress("UNUSED_PARAMETER")
actual fun CameraPreviewHost(
    mode: ScannerMode,
    torchOn: Boolean,
    captureNonce: Int,
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onStill: (CameraFrame) -> Unit,
    onPermission: (CameraPermissionStatus) -> Unit,
    onFailure: (AppFailure) -> Unit,
    modifier: Modifier
) {
    LaunchedEffect(Unit) {
        onPermission(CameraPermissionStatus.UNSUPPORTED)
    }
}
