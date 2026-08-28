package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.catalog.application.CatalogFreshness
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_catalog_apply
import com.hnexperts.cosmetics.resources.prefs_catalog_applied
import com.hnexperts.cosmetics.resources.prefs_catalog_check_action
import com.hnexperts.cosmetics.resources.prefs_catalog_notes_allergens
import com.hnexperts.cosmetics.resources.prefs_catalog_notes_annex
import com.hnexperts.cosmetics.resources.prefs_catalog_notes_microplastics
import com.hnexperts.cosmetics.resources.prefs_catalog_notes_tags
import com.hnexperts.cosmetics.resources.prefs_catalog_notes_title
import com.hnexperts.cosmetics.resources.prefs_catalog_offline
import com.hnexperts.cosmetics.resources.prefs_catalog_stamp
import com.hnexperts.cosmetics.resources.prefs_catalog_title
import com.hnexperts.cosmetics.resources.prefs_catalog_unknown
import com.hnexperts.cosmetics.resources.prefs_catalog_update
import com.hnexperts.cosmetics.resources.prefs_catalog_uptodate
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesCatalogSection(
    uiState: PreferencesUiState,
    onCheck: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(Res.string.prefs_catalog_title), style = MaterialTheme.typography.titleMedium)
        CatalogStamp(uiState)
        CatalogNotes()
        CatalogFreshnessBlock(uiState = uiState, onApply = onApply)
        if (uiState.catalogApplied) {
            Text(text = stringResource(Res.string.prefs_catalog_applied))
        }
        Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.prefs_catalog_check_action))
        }
    }
}

@Composable
private fun CatalogStamp(uiState: PreferencesUiState) {
    val meta = uiState.catalogMeta
    if (meta == null) {
        Text(text = stringResource(Res.string.prefs_catalog_unknown))
        return
    }
    Text(text = stringResource(Res.string.prefs_catalog_stamp, meta.catalogVersion, meta.builtAt, meta.region))
}

@Composable
private fun CatalogNotes() {
    Text(
        text = stringResource(Res.string.prefs_catalog_notes_title),
        style = MaterialTheme.typography.titleSmall
    )
    Text(
        text = stringResource(Res.string.prefs_catalog_notes_allergens),
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        text = stringResource(Res.string.prefs_catalog_notes_microplastics),
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        text = stringResource(Res.string.prefs_catalog_notes_annex),
        style = MaterialTheme.typography.bodySmall
    )
    Text(
        text = stringResource(Res.string.prefs_catalog_notes_tags),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun CatalogFreshnessBlock(uiState: PreferencesUiState, onApply: () -> Unit) {
    when (val freshness: CatalogFreshness? = uiState.freshness) {
        is CatalogFreshness.UpToDate -> Text(text = stringResource(Res.string.prefs_catalog_uptodate))
        is CatalogFreshness.UpdateAvailable -> {
            Text(text = stringResource(Res.string.prefs_catalog_update, freshness.published.catalogVersion))
            Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.prefs_catalog_apply))
            }
        }
        CatalogFreshness.Offline -> Text(text = stringResource(Res.string.prefs_catalog_offline))
        null -> Unit
    }
}
