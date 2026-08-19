package com.hnexperts.cosmetics.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hnexperts.cosmetics.hazards.domain.DangerLevel

/**
 * Rating semantics are fixed brand colours. They must not follow
 * Material You dynamic colour or the dark scheme: green always means
 * acceptable, red always means prohibited.
 */
object RatingColors {
    val safe: Color = Color(0xFF2E7D32)
    val low: Color = Color(0xFF558B2F)
    val moderate: Color = Color(0xFFF9A825)
    val restricted: Color = Color(0xFFEF6C00)
    val high: Color = Color(0xFFD84315)
    val prohibited: Color = Color(0xFFB71C1C)
    val unknown: Color = Color(0xFF546E7A)

    fun of(level: DangerLevel): Color {
        return when (level) {
            DangerLevel.SAFE -> safe
            DangerLevel.LOW -> low
            DangerLevel.MODERATE -> moderate
            DangerLevel.RESTRICTED -> restricted
            DangerLevel.HIGH -> high
            DangerLevel.PROHIBITED -> prohibited
            DangerLevel.UNKNOWN -> unknown
        }
    }

    /** Text colour that stays readable on the rating colour. */
    fun onColor(level: DangerLevel): Color {
        return when (level) {
            DangerLevel.MODERATE -> Color(0xFF201A00)
            else -> Color.White
        }
    }
}

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF00796B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00332E),
    secondary = Color(0xFF546E7A),
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1A1C1B),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1B),
    surfaceVariant = Color(0xFFEFF1F0),
    onSurfaceVariant = Color(0xFF404944)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF00332E)
)

@Composable
fun CosmeticsTheme(content: @Composable () -> Unit) {
    val dark: Boolean = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
