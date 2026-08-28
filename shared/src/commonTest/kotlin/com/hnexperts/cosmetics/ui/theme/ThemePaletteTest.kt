package com.hnexperts.cosmetics.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemePaletteTest {
    @Test
    fun resolveDarkThemeFollowsPreferenceNotRatings() {
        assertFalse(resolveDarkTheme(ThemePreference.LIGHT, systemDark = true))
        assertFalse(resolveDarkTheme(ThemePreference.LIGHT, systemDark = false))
        assertTrue(resolveDarkTheme(ThemePreference.DARK, systemDark = true))
        assertTrue(resolveDarkTheme(ThemePreference.DARK, systemDark = false))
        assertTrue(resolveDarkTheme(ThemePreference.FOLLOW_SYSTEM, systemDark = true))
        assertFalse(resolveDarkTheme(ThemePreference.FOLLOW_SYSTEM, systemDark = false))
        assertEquals(Color(0xFF2E7D32), RatingColors.of(DangerLevel.SAFE))
        assertEquals(Color(0xFFB71C1C), RatingColors.of(DangerLevel.PROHIBITED))
    }

    @Test
    fun trafficLightsStayFixed() {
        assertEquals(Color(0xFF2E7D32), RatingColors.safe)
        assertEquals(Color(0xFFB71C1C), RatingColors.prohibited)
        assertEquals(Color(0xFF2E7D32), RatingColors.of(DangerLevel.SAFE))
        assertEquals(Color(0xFFB71C1C), RatingColors.of(DangerLevel.PROHIBITED))
        assertEquals(Color.White, RatingColors.onColor(DangerLevel.SAFE))
        assertEquals(Color(0xFF201A00), RatingColors.onColor(DangerLevel.MODERATE))
    }

    @Test
    fun darkSurfacesAreReadableAndDoNotRetintRatings() {
        val light = cosmeticsColorScheme(dark = false)
        val dark = cosmeticsColorScheme(dark = true)
        assertTrue(dark.background.luminance() < light.background.luminance())
        assertTrue(dark.surface.luminance() < light.surface.luminance())
        assertTrue(dark.onSurface.luminance() > dark.surface.luminance())
        assertEquals(RatingColors.of(DangerLevel.SAFE), Color(0xFF2E7D32))
        assertEquals(RatingColors.of(DangerLevel.HIGH), Color(0xFFD84315))
    }
}
