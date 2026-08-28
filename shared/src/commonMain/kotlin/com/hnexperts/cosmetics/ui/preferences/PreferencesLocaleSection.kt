package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_language
import com.hnexperts.cosmetics.resources.prefs_language_en
import com.hnexperts.cosmetics.resources.prefs_language_pl
import com.hnexperts.cosmetics.resources.prefs_language_system
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.ChoiceChip
import com.hnexperts.cosmetics.ui.common.ChoiceChipFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesLocaleSection(
    stored: StoredPreferences,
    onFollowSystem: () -> Unit,
    onPin: (AppLocale) -> Unit
) {
    val chips: List<ChoiceChip<String>> = listOf(
        ChoiceChip(
            value = "system",
            label = stringResource(Res.string.prefs_language_system),
            selected = stored.localePreference == LocalePreference.FOLLOW_SYSTEM
        ),
        ChoiceChip(
            value = "en",
            label = stringResource(Res.string.prefs_language_en),
            selected = stored.localePreference == LocalePreference.PINNED && stored.pinnedLocale?.language == "en"
        ),
        ChoiceChip(
            value = "pl",
            label = stringResource(Res.string.prefs_language_pl),
            selected = stored.localePreference == LocalePreference.PINNED && stored.pinnedLocale?.language == "pl"
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.prefs_language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.screenHeading()
        )
        ChoiceChipFlow(
            chips = chips,
            onSelect = { key ->
                when (key) {
                    "en" -> onPin(AppLocale.ENGLISH)
                    "pl" -> onPin(AppLocale.POLISH)
                    else -> onFollowSystem()
                }
            }
        )
    }
}
