package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_export_avoid
import com.hnexperts.cosmetics.resources.prefs_export_avoid_copied
import com.hnexperts.cosmetics.resources.prefs_export_avoid_empty
import com.hnexperts.cosmetics.resources.prefs_export_shelf
import com.hnexperts.cosmetics.resources.prefs_export_shelf_copied
import com.hnexperts.cosmetics.resources.prefs_export_shelf_empty
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesExportSection(
    avoidCopied: Boolean,
    shelfCopied: Boolean,
    onCopyAvoid: (String) -> Unit,
    onCopyShelf: (String) -> Unit
) {
    val emptyAvoid: String = stringResource(Res.string.prefs_export_avoid_empty)
    Button(
        onClick = { onCopyAvoid(emptyAvoid) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_export_avoid))
    }
    if (avoidCopied) {
        Text(text = stringResource(Res.string.prefs_export_avoid_copied))
    }
    val emptyShelf: String = stringResource(Res.string.prefs_export_shelf_empty)
    Button(
        onClick = { onCopyShelf(emptyShelf) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(Res.string.prefs_export_shelf))
    }
    if (shelfCopied) {
        Text(text = stringResource(Res.string.prefs_export_shelf_copied))
    }
}
