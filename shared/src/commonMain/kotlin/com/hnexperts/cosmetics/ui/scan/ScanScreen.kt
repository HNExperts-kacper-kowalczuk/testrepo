package com.hnexperts.cosmetics.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.scan_camera_note
import com.hnexperts.cosmetics.resources.scan_not_found_body
import com.hnexperts.cosmetics.resources.scan_not_found_title
import com.hnexperts.cosmetics.resources.scan_online_no_inci
import com.hnexperts.cosmetics.resources.scan_open_barcode
import com.hnexperts.cosmetics.resources.scan_open_inci
import com.hnexperts.cosmetics.resources.scan_recent_title
import com.hnexperts.cosmetics.resources.scan_title
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.chrome.AppActionIcons
import com.hnexperts.cosmetics.ui.chrome.ButtonIconLabel
import com.hnexperts.cosmetics.ui.common.BusyStatus
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.HistoryEntryCard
import com.hnexperts.cosmetics.ui.layout.AppScrollPane
import com.hnexperts.cosmetics.ui.motion.Reveal
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onResult: () -> Unit,
    onConfirm: () -> Unit,
    onOpenBarcodeCamera: () -> Unit,
    onOpenInciCamera: () -> Unit
) {
    val uiState: ScanUiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshRecent()
    }
    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onResult()
            viewModel.consumeNavigation()
        }
    }
    LaunchedEffect(uiState.navigateToConfirm) {
        if (uiState.navigateToConfirm) {
            onConfirm()
            viewModel.consumeNavigation()
        }
    }

    AppScrollPane(modifier = Modifier.statusBarsPadding()) {
        Text(
            text = stringResource(Res.string.scan_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.screenHeading()
        )
        FailureBanner(failure = uiState.failure)
        Button(
            onClick = onOpenBarcodeCamera,
            enabled = !uiState.busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            ButtonIconLabel(
                imageVector = AppActionIcons.Barcode,
                text = stringResource(Res.string.scan_open_barcode)
            )
        }
        OutlinedButton(
            onClick = onOpenInciCamera,
            enabled = !uiState.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonIconLabel(
                imageVector = AppActionIcons.Document,
                text = stringResource(Res.string.scan_open_inci)
            )
        }
        Text(
            text = stringResource(Res.string.scan_camera_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Reveal(visible = uiState.notFoundGtin != null) {
            NotFoundCard(
                busy = uiState.busy,
                onlineNoIngredients = uiState.onlineNoIngredients,
                onOpenInciCamera = onOpenInciCamera
            )
        }
        Reveal(visible = uiState.busy) {
            BusyStatus(message = stringResource(Res.string.scan_working))
        }
        RecentScans(entries = uiState.recent, busy = uiState.busy, onOpen = viewModel::reopen)
        ManualEntrySection(viewModel = viewModel, uiState = uiState)
    }
}

@Composable
private fun NotFoundCard(
    busy: Boolean,
    onlineNoIngredients: Boolean,
    onOpenInciCamera: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(Res.string.scan_not_found_title), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(Res.string.scan_not_found_body), style = MaterialTheme.typography.bodyMedium)
            if (onlineNoIngredients) {
                Text(text = stringResource(Res.string.scan_online_no_inci), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = onOpenInciCamera,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                ButtonIconLabel(
                    imageVector = AppActionIcons.Document,
                    text = stringResource(Res.string.scan_open_inci)
                )
            }
        }
    }
}

@Composable
private fun RecentScans(
    entries: List<HistoryEntry>,
    busy: Boolean,
    onOpen: (HistoryEntry) -> Unit
) {
    if (entries.isEmpty()) {
        return
    }
    Text(
        text = stringResource(Res.string.scan_recent_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.screenHeading()
    )
    for (entry in entries) {
        HistoryEntryCard(entry = entry, enabled = !busy, onOpen = onOpen)
    }
}
