package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_alcohol_leave_on
import com.hnexperts.cosmetics.resources.prefs_avoid_search
import com.hnexperts.cosmetics.resources.prefs_avoid_title
import com.hnexperts.cosmetics.resources.prefs_children_caution
import com.hnexperts.cosmetics.resources.prefs_essential_oil
import com.hnexperts.cosmetics.resources.prefs_eu_allergens
import com.hnexperts.cosmetics.resources.prefs_eu_allergens_hint
import com.hnexperts.cosmetics.resources.prefs_fragrance_free
import com.hnexperts.cosmetics.resources.prefs_pregnancy
import com.hnexperts.cosmetics.resources.prefs_privacy
import com.hnexperts.cosmetics.resources.prefs_remove_ads
import com.hnexperts.cosmetics.resources.prefs_remove_ads_unavailable
import com.hnexperts.cosmetics.resources.prefs_ads_removed
import com.hnexperts.cosmetics.resources.prefs_title
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.PreferenceToggleRow
import com.hnexperts.cosmetics.ui.layout.AppScrollPane
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesScreen(viewModel: PreferencesViewModel) {
    val uiState: PreferencesUiState by viewModel.uiState.collectAsState()
    val stored: StoredPreferences = uiState.stored
    AppScrollPane(modifier = Modifier.statusBarsPadding()) {
        Text(
            text = stringResource(Res.string.prefs_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.screenHeading()
        )
        FailureBanner(failure = uiState.failure, onRetry = viewModel::reload)
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_pregnancy),
            checked = stored.profile.pregnancyCaution,
            onCheckedChange = viewModel::setPregnancyCaution
        )
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_fragrance_free),
            checked = stored.profile.fragranceFree,
            onCheckedChange = viewModel::setFragranceFree
        )
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_eu_allergens),
            checked = stored.profile.euAllergens,
            onCheckedChange = viewModel::setEuAllergens
        )
        Text(
            text = stringResource(Res.string.prefs_eu_allergens_hint),
            style = MaterialTheme.typography.bodySmall
        )
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_children_caution),
            checked = stored.profile.childrenCaution,
            onCheckedChange = viewModel::setChildrenCaution
        )
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_alcohol_leave_on),
            checked = stored.profile.alcoholLeaveOn,
            onCheckedChange = viewModel::setAlcoholLeaveOn
        )
        PreferenceToggleRow(
            label = stringResource(Res.string.prefs_essential_oil),
            checked = stored.profile.essentialOilCluster,
            onCheckedChange = viewModel::setEssentialOilCluster
        )
        PreferencesLocaleSection(
            stored = stored,
            onFollowSystem = viewModel::setFollowSystemLocale,
            onPin = viewModel::pinLocale
        )
        PreferencesThemeSection(
            preference = stored.themePreference,
            onFollowSystem = viewModel::setFollowSystemTheme,
            onLight = viewModel::setLightTheme,
            onDark = viewModel::setDarkTheme
        )
        Text(
            text = stringResource(Res.string.prefs_avoid_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.screenHeading()
        )
        OutlinedTextField(
            value = uiState.avoidQuery,
            onValueChange = viewModel::setAvoidQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.prefs_avoid_search)) },
            singleLine = true
        )
        uiState.ingredients.forEach { ingredient ->
            PreferenceToggleRow(
                label = ingredient.inciName,
                checked = stored.profile.avoidedIngredientIds.contains(ingredient.id),
                onCheckedChange = { viewModel.toggleAvoid(ingredient.id) }
            )
        }
        PreferencesCatalogSection(
            uiState = uiState,
            onCheck = viewModel::reload,
            onApply = viewModel::applyCatalogUpdate
        )
        PreferencesReportsSection(
            openReportCount = uiState.openReportCount,
            reportsCopied = uiState.reportsCopied,
            reportsSent = uiState.reportsSent,
            reportsSendAvailable = uiState.reportsSendAvailable,
            onCopyReports = viewModel::copyReports,
            onSendReports = viewModel::sendReports
        )
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
