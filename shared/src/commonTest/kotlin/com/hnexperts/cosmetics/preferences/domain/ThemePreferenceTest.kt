package com.hnexperts.cosmetics.preferences.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferenceTest {
    @Test
    fun storageRoundTrip() {
        ThemePreference.entries.forEach { preference ->
            assertEquals(preference, ThemePreference.fromStorage(preference.storageValue()))
        }
    }

    @Test
    fun unknownStorageFollowsSystem() {
        assertEquals(ThemePreference.FOLLOW_SYSTEM, ThemePreference.fromStorage(null))
        assertEquals(ThemePreference.FOLLOW_SYSTEM, ThemePreference.fromStorage("system"))
        assertEquals(ThemePreference.FOLLOW_SYSTEM, ThemePreference.fromStorage("other"))
    }
}
