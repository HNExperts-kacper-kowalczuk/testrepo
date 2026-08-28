package com.hnexperts.cosmetics.ui.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.prefs_theme
import com.hnexperts.cosmetics.resources.prefs_theme_dark
import com.hnexperts.cosmetics.resources.prefs_theme_light
import com.hnexperts.cosmetics.resources.prefs_theme_system
import org.jetbrains.compose.resources.stringResource

@Composable
fun PreferencesThemeSection(
    preference: ThemePreference,
    onFollowSystem: () -> Unit,
    onLight: () -> Unit,
    onDark: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(Res.string.prefs_theme), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = preference == ThemePreference.FOLLOW_SYSTEM,
                onClick = onFollowSystem,
                label = { Text(stringResource(Res.string.prefs_theme_system)) }
            )
            FilterChip(
                selected = preference == ThemePreference.LIGHT,
                onClick = onLight,
                label = { Text(stringResource(Res.string.prefs_theme_light)) }
            )
            FilterChip(
                selected = preference == ThemePreference.DARK,
                onClick = onDark,
                label = { Text(stringResource(Res.string.prefs_theme_dark)) }
            )
        }
    }
}
