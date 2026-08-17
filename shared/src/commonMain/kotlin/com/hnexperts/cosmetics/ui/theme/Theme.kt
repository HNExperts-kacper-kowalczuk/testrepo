package com.hnexperts.cosmetics.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import com.hnexperts.cosmetics.hazards.domain.DangerLevel

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
}

@Composable
fun CosmeticsTheme(content: @Composable () -> Unit) {
    val dark: Boolean = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
        content = content
    )
}
