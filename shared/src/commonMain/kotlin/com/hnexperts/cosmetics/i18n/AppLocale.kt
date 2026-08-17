package com.hnexperts.cosmetics.i18n

data class AppLocale(
    val language: String,
    val region: String? = null
) {
    val tag: String
        get() {
            val regionValue: String? = region
            if (regionValue == null) {
                return language
            }
            return "$language-$regionValue"
        }

    companion object {
        val ENGLISH: AppLocale = AppLocale("en")
        val POLISH: AppLocale = AppLocale("pl")

        fun parse(tag: String): AppLocale {
            val parts: List<String> = tag.split('-', '_')
            val language: String = parts[0].lowercase()
            val region: String? = parts.getOrNull(1)?.uppercase()
            return AppLocale(language, region)
        }
    }
}

enum class LocalePreference {
    FOLLOW_SYSTEM,
    PINNED
}

class CommentLocalizer {
    fun pick(rows: List<com.hnexperts.cosmetics.hazards.domain.LocalizedText>, requested: AppLocale): com.hnexperts.cosmetics.hazards.domain.LocalizedText? {
        if (rows.isEmpty()) {
            return null
        }
        val exact: com.hnexperts.cosmetics.hazards.domain.LocalizedText? =
            rows.firstOrNull { row -> row.locale.equals(requested.tag, ignoreCase = true) }
        if (exact != null) {
            return exact
        }
        val language: com.hnexperts.cosmetics.hazards.domain.LocalizedText? =
            rows.firstOrNull { row -> row.locale.equals(requested.language, ignoreCase = true) }
        if (language != null) {
            return language
        }
        val english: com.hnexperts.cosmetics.hazards.domain.LocalizedText? =
            rows.firstOrNull { row -> row.locale.equals("en", ignoreCase = true) }
        if (english != null) {
            return english
        }
        return rows.first()
    }
}
