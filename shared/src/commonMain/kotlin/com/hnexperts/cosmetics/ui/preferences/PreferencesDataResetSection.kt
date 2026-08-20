package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.prefs_clear_avoid
import com.hnexperts.cosmetics.resources.prefs_clear_avoid_body
import com.hnexperts.cosmetics.resources.prefs_clear_history
import com.hnexperts.cosmetics.resources.prefs_clear_history_body
import com.hnexperts.cosmetics.resources.prefs_clear_shelf
import com.hnexperts.cosmetics.resources.prefs_clear_shelf_body
import com.hnexperts.cosmetics.resources.prefs_cleared_avoid
import com.hnexperts.cosmetics.resources.prefs_cleared_history
import com.hnexperts.cosmetics.resources.prefs_cleared_shelf
import com.hnexperts.cosmetics.resources.prefs_reset_confirm
import com.hnexperts.cosmetics.resources.prefs_reset_device
import com.hnexperts.cosmetics.resources.prefs_reset_device_body
import com.hnexperts.cosmetics.resources.prefs_reset_device_done
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesDataResetSection(
    pendingReset: DataResetKind?,
    cleared: DataResetKind?,
    onRequestReset: (DataResetKind) -> Unit,
    onCancelReset: () -> Unit,
    onConfirmReset: () -> Unit
) {
    Button(
        onClick = { onRequestReset(DataResetKind.HISTORY) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_clear_history))
    }
    Button(
        onClick = { onRequestReset(DataResetKind.SHELF) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_clear_shelf))
    }
    Button(
        onClick = { onRequestReset(DataResetKind.AVOID_LIST) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_clear_avoid))
    }
    Button(
        onClick = { onRequestReset(DataResetKind.DEVICE) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_reset_device))
    }
    ClearedMessage(cleared)
    if (pendingReset != null) {
        ResetConfirmDialog(
            kind = pendingReset,
            onConfirm = onConfirmReset,
            onCancel = onCancelReset
        )
    }
}

@Composable
private fun ClearedMessage(cleared: DataResetKind?) {
    val message: String? = when (cleared) {
        DataResetKind.HISTORY -> stringResource(Res.string.prefs_cleared_history)
        DataResetKind.SHELF -> stringResource(Res.string.prefs_cleared_shelf)
        DataResetKind.AVOID_LIST -> stringResource(Res.string.prefs_cleared_avoid)
        DataResetKind.DEVICE -> stringResource(Res.string.prefs_reset_device_done)
        null -> null
    }
    if (message != null) {
        Text(text = message)
    }
}

@Composable
private fun ResetConfirmDialog(
    kind: DataResetKind,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(resetTitle(kind)) },
        text = { Text(resetBody(kind)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.prefs_reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(Res.string.back))
            }
        }
    )
}

@Composable
private fun resetTitle(kind: DataResetKind): String {
    return when (kind) {
        DataResetKind.HISTORY -> stringResource(Res.string.prefs_clear_history)
        DataResetKind.SHELF -> stringResource(Res.string.prefs_clear_shelf)
        DataResetKind.AVOID_LIST -> stringResource(Res.string.prefs_clear_avoid)
        DataResetKind.DEVICE -> stringResource(Res.string.prefs_reset_device)
    }
}

@Composable
private fun resetBody(kind: DataResetKind): String {
    return when (kind) {
        DataResetKind.HISTORY -> stringResource(Res.string.prefs_clear_history_body)
        DataResetKind.SHELF -> stringResource(Res.string.prefs_clear_shelf_body)
        DataResetKind.AVOID_LIST -> stringResource(Res.string.prefs_clear_avoid_body)
        DataResetKind.DEVICE -> stringResource(Res.string.prefs_reset_device_body)
    }
}
