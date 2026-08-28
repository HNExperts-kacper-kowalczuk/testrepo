package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_theme
import com.hnexperts.cosmetics.resources.prefs_theme_dark
import com.hnexperts.cosmetics.resources.prefs_theme_light
import com.hnexperts.cosmetics.resources.prefs_theme_system
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.ChoiceChip
import com.hnexperts.cosmetics.ui.common.ChoiceChipFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesThemeSection(
    preference: ThemePreference,
    onFollowSystem: () -> Unit,
    onLight: () -> Unit,
    onDark: () -> Unit
) {
    val chips: List<ChoiceChip<ThemePreference>> = listOf(
        ChoiceChip(
            ThemePreference.FOLLOW_SYSTEM,
            stringResource(Res.string.prefs_theme_system),
            preference == ThemePreference.FOLLOW_SYSTEM
        ),
        ChoiceChip(
            ThemePreference.LIGHT,
            stringResource(Res.string.prefs_theme_light),
            preference == ThemePreference.LIGHT
        ),
        ChoiceChip(
            ThemePreference.DARK,
            stringResource(Res.string.prefs_theme_dark),
            preference == ThemePreference.DARK
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.prefs_theme),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.screenHeading()
        )
        ChoiceChipFlow(
            chips = chips,
            onSelect = { next ->
                when (next) {
                    ThemePreference.FOLLOW_SYSTEM -> onFollowSystem()
                    ThemePreference.LIGHT -> onLight()
                    ThemePreference.DARK -> onDark()
                }
            }
        )
    }
}
