package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_export_avoid
import com.hnexperts.cosmetics.resources.prefs_export_avoid_copied
import com.hnexperts.cosmetics.resources.prefs_export_avoid_empty
import com.hnexperts.cosmetics.resources.prefs_export_shelf
import com.hnexperts.cosmetics.resources.prefs_export_shelf_copied
import com.hnexperts.cosmetics.resources.prefs_export_shelf_empty
import com.hnexperts.cosmetics.ui.chrome.AppActionIcons
import com.hnexperts.cosmetics.ui.chrome.ButtonIconLabel
import com.hnexperts.cosmetics.ui.common.StatusAnnouncement
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
        ButtonIconLabel(
            imageVector = AppActionIcons.Copy,
            text = stringResource(Res.string.prefs_export_avoid)
        )
    }
    StatusAnnouncement(
        message = if (avoidCopied) stringResource(Res.string.prefs_export_avoid_copied) else null
    )
    val emptyShelf: String = stringResource(Res.string.prefs_export_shelf_empty)
    Button(
        onClick = { onCopyShelf(emptyShelf) },
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonIconLabel(
            imageVector = AppActionIcons.Copy,
            text = stringResource(Res.string.prefs_export_shelf)
        )
    }
    StatusAnnouncement(
        message = if (shelfCopied) stringResource(Res.string.prefs_export_shelf_copied) else null
    )
}
