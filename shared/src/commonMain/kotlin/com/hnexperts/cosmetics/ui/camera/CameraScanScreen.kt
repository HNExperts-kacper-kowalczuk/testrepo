package com.hnexperts.cosmetics.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.platform.openAppSettings
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.camera_capture
import com.hnexperts.cosmetics.resources.camera_hint_barcode
import com.hnexperts.cosmetics.resources.camera_hint_inci
import com.hnexperts.cosmetics.resources.camera_permission_body
import com.hnexperts.cosmetics.resources.camera_permission_request
import com.hnexperts.cosmetics.resources.camera_permission_settings
import com.hnexperts.cosmetics.resources.camera_permission_title
import com.hnexperts.cosmetics.resources.camera_title_barcode
import com.hnexperts.cosmetics.resources.camera_title_inci
import com.hnexperts.cosmetics.resources.camera_torch_off
import com.hnexperts.cosmetics.resources.camera_torch_on
import com.hnexperts.cosmetics.resources.camera_unavailable
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import com.hnexperts.cosmetics.ui.common.FailureBanner
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    viewModel: CameraScanViewModel,
    onBack: () -> Unit,
    onResult: () -> Unit,
    onConfirm: () -> Unit
) {
    val uiState: CameraScanUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.navigateToResult, uiState.navigateToConfirm, uiState.navigateBackNotFound) {
        when {
            uiState.navigateToResult -> {
                onResult()
                viewModel.consumeNavigation()
            }
            uiState.navigateToConfirm -> {
                onConfirm()
                viewModel.consumeNavigation()
            }
            uiState.navigateBackNotFound -> {
                onBack()
                viewModel.consumeNavigation()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.mode == ScannerMode.BARCODE) {
                            stringResource(Res.string.camera_title_barcode)
                        } else {
                            stringResource(Res.string.camera_title_inci)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FailureBanner(failure = uiState.failure)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.permission != CameraPermissionStatus.UNSUPPORTED) {
                    CameraPreviewHost(
                        mode = uiState.mode,
                        torchOn = uiState.torchOn,
                        captureNonce = uiState.captureNonce,
                        enabled = !uiState.busy && uiState.permission == CameraPermissionStatus.GRANTED,
                        onBarcode = viewModel::onBarcode,
                        onStill = viewModel::onStillCaptured,
                        onPermission = viewModel::onPermission,
                        onFailure = viewModel::onCameraFailure,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                PermissionOverlay(status = uiState.permission)
            }
            CameraControls(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun PermissionOverlay(status: CameraPermissionStatus) {
    when (status) {
        CameraPermissionStatus.GRANTED, CameraPermissionStatus.UNKNOWN -> Unit
        CameraPermissionStatus.UNSUPPORTED -> {
            Text(
                text = stringResource(Res.string.camera_unavailable),
                modifier = Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.surface)
            )
        }
        CameraPermissionStatus.DENIED, CameraPermissionStatus.PERMANENTLY_DENIED -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(Res.string.camera_permission_title), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(Res.string.camera_permission_body))
                if (status == CameraPermissionStatus.PERMANENTLY_DENIED) {
                    Button(onClick = { openAppSettings() }) {
                        Text(stringResource(Res.string.camera_permission_settings))
                    }
                } else {
                    Text(text = stringResource(Res.string.camera_permission_request))
                }
            }
        }
    }
}

@Composable
private fun CameraControls(
    uiState: CameraScanUiState,
    viewModel: CameraScanViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.mode == ScannerMode.BARCODE) {
                stringResource(Res.string.camera_hint_barcode)
            } else {
                stringResource(Res.string.camera_hint_inci)
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = viewModel::toggleTorch, enabled = uiState.permission == CameraPermissionStatus.GRANTED) {
                Text(
                    text = if (uiState.torchOn) {
                        stringResource(Res.string.camera_torch_on)
                    } else {
                        stringResource(Res.string.camera_torch_off)
                    }
                )
            }
            if (uiState.mode == ScannerMode.INGREDIENT_LIST) {
                Button(
                    onClick = viewModel::captureStill,
                    enabled = uiState.permission == CameraPermissionStatus.GRANTED && !uiState.busy
                ) {
                    Text(stringResource(Res.string.camera_capture))
                }
            }
        }
        if (uiState.busy) {
            CircularProgressIndicator()
            Text(text = stringResource(Res.string.scan_working))
        }
    }
}
