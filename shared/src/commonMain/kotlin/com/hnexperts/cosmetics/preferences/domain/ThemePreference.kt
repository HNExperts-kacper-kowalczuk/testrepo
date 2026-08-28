package com.hnexperts.cosmetics.preferences.domain

enum class ThemePreference {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
    ;

    fun storageValue(): String {
        return when (this) {
            FOLLOW_SYSTEM -> "system"
            LIGHT -> "light"
            DARK -> "dark"
        }
    }

    companion object {
        fun fromStorage(value: String?): ThemePreference {
            return when (value) {
                "light" -> LIGHT
                "dark" -> DARK
                else -> FOLLOW_SYSTEM
            }
        }
    }
}
