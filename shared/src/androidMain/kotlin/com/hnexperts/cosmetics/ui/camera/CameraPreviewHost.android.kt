package com.hnexperts.cosmetics.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.android.AndroidCameraSession
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.ScannerMode

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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView: PreviewView = remember {
        PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE }
    }
    val session: AndroidCameraSession = remember {
        AndroidCameraSession(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onBarcode = onBarcode,
            onStill = onStill,
            onFailure = onFailure
        )
    }
    var granted: Boolean by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        granted = isGranted
        onPermission(permissionStatus(context, isGranted))
    }
    LaunchedEffect(Unit) {
        val alreadyGranted: Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            granted = true
            onPermission(CameraPermissionStatus.GRANTED)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(mode, granted) {
        if (granted) {
            session.bind(mode, torchOn)
        }
    }
    LaunchedEffect(torchOn) {
        session.setTorch(torchOn)
    }
    LaunchedEffect(enabled) {
        session.setBarcodeListening(enabled)
    }
    LaunchedEffect(captureNonce) {
        session.captureIfNeeded(captureNonce)
    }
    DisposableEffect(session) {
        onDispose { session.release() }
    }
    AndroidView(factory = { previewView }, modifier = modifier)
}

private fun permissionStatus(context: android.content.Context, granted: Boolean): CameraPermissionStatus {
    if (granted) {
        return CameraPermissionStatus.GRANTED
    }
    val activity = context as? android.app.Activity
    val showRationale: Boolean = activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true
    return if (showRationale) {
        CameraPermissionStatus.DENIED
    } else {
        CameraPermissionStatus.PERMANENTLY_DENIED
    }
}
