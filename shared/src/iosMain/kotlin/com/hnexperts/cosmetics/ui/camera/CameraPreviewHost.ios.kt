package com.hnexperts.cosmetics.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import com.hnexperts.cosmetics.scanning.ios.IosCameraSession
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
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
    val session: IosCameraSession = remember {
        IosCameraSession(onBarcode = onBarcode, onStill = onStill, onFailure = onFailure)
    }
    LaunchedEffect(Unit) {
        requestCameraPermission { status ->
            onPermission(status)
            if (status == CameraPermissionStatus.GRANTED) {
                session.start()
            }
        }
    }
    LaunchedEffect(enabled, mode) {
        session.setBarcodeListening(enabled && mode == ScannerMode.BARCODE)
    }
    LaunchedEffect(torchOn) {
        session.setTorch(torchOn)
    }
    LaunchedEffect(captureNonce) {
        session.captureIfNeeded(captureNonce)
    }
    DisposableEffect(session) {
        onDispose { session.release() }
    }
    UIKitView(
        factory = {
            val view = UIView()
            session.attach(view)
            view
        },
        modifier = modifier,
        update = { view -> session.layout(view) }
    )
}

private fun requestCameraPermission(onPermission: (CameraPermissionStatus) -> Unit) {
    val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    when (status) {
        AVAuthorizationStatusAuthorized -> onPermission(CameraPermissionStatus.GRANTED)
        AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted ->
            onPermission(CameraPermissionStatus.PERMANENTLY_DENIED)
        else -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            onPermission(
                if (granted) CameraPermissionStatus.GRANTED else CameraPermissionStatus.DENIED
            )
        }
    }
}
