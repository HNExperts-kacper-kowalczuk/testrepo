package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.data.StoredPreferences
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_avoid_title
import com.hnexperts.cosmetics.resources.prefs_fragrance_free
import com.hnexperts.cosmetics.resources.prefs_language
import com.hnexperts.cosmetics.resources.prefs_language_en
import com.hnexperts.cosmetics.resources.prefs_language_pl
import com.hnexperts.cosmetics.resources.prefs_language_system
import com.hnexperts.cosmetics.resources.prefs_pregnancy
import com.hnexperts.cosmetics.resources.prefs_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesScreen(viewModel: PreferencesViewModel) {
    val stored: StoredPreferences by viewModel.preferences.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(Res.string.prefs_title), style = MaterialTheme.typography.headlineSmall)
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
        ingredients.forEach { ingredient ->
            PreferenceSwitch(
                label = ingredient.inciName,
                checked = stored.profile.avoidedIngredientIds.contains(ingredient.id),
                onCheckedChange = { viewModel.toggleAvoid(ingredient.id) }
            )
        }
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
