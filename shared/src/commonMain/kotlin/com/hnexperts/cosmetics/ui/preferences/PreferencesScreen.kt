package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.catalog.application.CatalogFreshness
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_alcohol_leave_on
import com.hnexperts.cosmetics.resources.prefs_avoid_search
import com.hnexperts.cosmetics.resources.prefs_avoid_title
import com.hnexperts.cosmetics.resources.prefs_children_caution
import com.hnexperts.cosmetics.resources.prefs_catalog_apply
import com.hnexperts.cosmetics.resources.prefs_catalog_applied
import com.hnexperts.cosmetics.resources.prefs_catalog_check_action
import com.hnexperts.cosmetics.resources.prefs_catalog_offline
import com.hnexperts.cosmetics.resources.prefs_catalog_stamp
import com.hnexperts.cosmetics.resources.prefs_catalog_title
import com.hnexperts.cosmetics.resources.prefs_catalog_unknown
import com.hnexperts.cosmetics.resources.prefs_catalog_update
import com.hnexperts.cosmetics.resources.prefs_catalog_uptodate
import com.hnexperts.cosmetics.resources.prefs_essential_oil
import com.hnexperts.cosmetics.resources.prefs_eu_allergens
import com.hnexperts.cosmetics.resources.prefs_eu_allergens_hint
import com.hnexperts.cosmetics.resources.prefs_fragrance_free
import com.hnexperts.cosmetics.resources.prefs_language
import com.hnexperts.cosmetics.resources.prefs_language_en
import com.hnexperts.cosmetics.resources.prefs_language_pl
import com.hnexperts.cosmetics.resources.prefs_language_system
import com.hnexperts.cosmetics.resources.prefs_pregnancy
import com.hnexperts.cosmetics.resources.prefs_privacy
import com.hnexperts.cosmetics.resources.prefs_remove_ads
import com.hnexperts.cosmetics.resources.prefs_remove_ads_unavailable
import com.hnexperts.cosmetics.resources.prefs_reports_copy
import com.hnexperts.cosmetics.resources.prefs_reports_copied
import com.hnexperts.cosmetics.resources.prefs_reports_count
import com.hnexperts.cosmetics.resources.prefs_reports_empty
import com.hnexperts.cosmetics.resources.prefs_ads_removed
import com.hnexperts.cosmetics.resources.prefs_title
import com.hnexperts.cosmetics.ui.common.FailureBanner
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesScreen(viewModel: PreferencesViewModel) {
    val uiState: PreferencesUiState by viewModel.uiState.collectAsState()
    val stored: StoredPreferences = uiState.stored
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(Res.string.prefs_title), style = MaterialTheme.typography.headlineSmall)
        FailureBanner(failure = uiState.failure, onRetry = viewModel::reload)
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_pregnancy),
            checked = stored.profile.pregnancyCaution,
            onCheckedChange = viewModel::setPregnancyCaution
        )
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_fragrance_free),
            checked = stored.profile.fragranceFree,
            onCheckedChange = viewModel::setFragranceFree
        )
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_eu_allergens),
            checked = stored.profile.euAllergens,
            onCheckedChange = viewModel::setEuAllergens
        )
        Text(
            text = stringResource(Res.string.prefs_eu_allergens_hint),
            style = MaterialTheme.typography.bodySmall
        )
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_children_caution),
            checked = stored.profile.childrenCaution,
            onCheckedChange = viewModel::setChildrenCaution
        )
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_alcohol_leave_on),
            checked = stored.profile.alcoholLeaveOn,
            onCheckedChange = viewModel::setAlcoholLeaveOn
        )
        PreferenceSwitch(
            label = stringResource(Res.string.prefs_essential_oil),
            checked = stored.profile.essentialOilCluster,
            onCheckedChange = viewModel::setEssentialOilCluster
        )
        Text(text = stringResource(Res.string.prefs_language), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = stored.localePreference == LocalePreference.FOLLOW_SYSTEM,
                onClick = viewModel::setFollowSystemLocale,
                label = { Text(stringResource(Res.string.prefs_language_system)) }
            )
            FilterChip(
                selected = stored.localePreference == LocalePreference.PINNED && stored.pinnedLocale?.language == "en",
                onClick = { viewModel.pinLocale(AppLocale.ENGLISH) },
                label = { Text(stringResource(Res.string.prefs_language_en)) }
            )
            FilterChip(
                selected = stored.localePreference == LocalePreference.PINNED && stored.pinnedLocale?.language == "pl",
                onClick = { viewModel.pinLocale(AppLocale.POLISH) },
                label = { Text(stringResource(Res.string.prefs_language_pl)) }
            )
        }
        Text(text = stringResource(Res.string.prefs_avoid_title), style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = uiState.avoidQuery,
            onValueChange = viewModel::setAvoidQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.prefs_avoid_search)) },
            singleLine = true
        )
        uiState.ingredients.forEach { ingredient ->
            PreferenceSwitch(
                label = ingredient.inciName,
                checked = stored.profile.avoidedIngredientIds.contains(ingredient.id),
                onCheckedChange = { viewModel.toggleAvoid(ingredient.id) }
            )
        }
        CatalogSection(
            uiState = uiState,
            onCheck = viewModel::reload,
            onApply = viewModel::applyCatalogUpdate
        )
        Text(
            text = stringResource(Res.string.prefs_reports_count, uiState.openReportCount.toString()),
            style = MaterialTheme.typography.bodyLarge
        )
        val emptyReports: String = stringResource(Res.string.prefs_reports_empty)
        Button(
            onClick = { viewModel.copyReports(emptyReports) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.prefs_reports_copy))
        }
        if (uiState.reportsCopied) {
            Text(text = stringResource(Res.string.prefs_reports_copied))
        }
        PreferencesExportSection(
            avoidCopied = uiState.avoidCopied,
            shelfCopied = uiState.shelfCopied,
            onCopyAvoid = viewModel::copyAvoidList,
            onCopyShelf = viewModel::copyShelf
        )
        AdsPurchaseSection(
            adsRemoved = uiState.adsRemoved,
            billingAvailable = uiState.billingAvailable,
            onPurchase = viewModel::purchaseRemoveAds
        )
        if (uiState.ads.privacyOptionsRequired) {
            Button(onClick = viewModel::openPrivacyOptions, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.prefs_privacy))
            }
        }
        PreferencesDataResetSection(
            pendingReset = uiState.pendingReset,
            cleared = uiState.cleared,
            onRequestReset = viewModel::requestReset,
            onCancelReset = viewModel::cancelReset,
            onConfirmReset = viewModel::confirmReset
        )
    }
}

@Composable
private fun AdsPurchaseSection(
    adsRemoved: Boolean,
    billingAvailable: Boolean,
    onPurchase: () -> Unit
) {
    if (adsRemoved) {
        Text(text = stringResource(Res.string.prefs_ads_removed))
        return
    }
    if (billingAvailable) {
        Button(onClick = onPurchase, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.prefs_remove_ads))
        }
        return
    }
    Text(text = stringResource(Res.string.prefs_remove_ads_unavailable))
}

@Composable
private fun CatalogSection(
    uiState: PreferencesUiState,
    onCheck: () -> Unit,
    onApply: () -> Unit
) {
    Text(text = stringResource(Res.string.prefs_catalog_title), style = MaterialTheme.typography.titleMedium)
    val meta = uiState.catalogMeta
    if (meta == null) {
        Text(text = stringResource(Res.string.prefs_catalog_unknown))
    } else {
        Text(text = stringResource(Res.string.prefs_catalog_stamp, meta.catalogVersion, meta.builtAt, meta.region))
    }
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
    if (uiState.catalogApplied) {
        Text(text = stringResource(Res.string.prefs_catalog_applied))
    }
    Button(onClick = onCheck, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.prefs_catalog_check_action))
    }
}

@Composable
private fun PreferenceSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
