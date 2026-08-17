package com.hnexperts.cosmetics.i18n

import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentLocalizerTest {
    private val localizer: CommentLocalizer = CommentLocalizer()
    private val rows: List<LocalizedText> = listOf(
        LocalizedText("en", "English summary", null),
        LocalizedText("pl", "Polskie streszczenie", null)
    )

    @Test
    fun picksPolishWhenRequested() {
        val picked: LocalizedText? = localizer.pick(rows, AppLocale.POLISH)
        assertEquals("pl", picked?.locale)
    }

    @Test
    fun fallsBackToEnglishThenAny() {
        val germanRequest: LocalizedText? = localizer.pick(rows, AppLocale.parse("de"))
        assertEquals("en", germanRequest?.locale)

        val onlyPolish: LocalizedText? = localizer.pick(
            listOf(LocalizedText("pl", "Tylko polski", null)),
            AppLocale.ENGLISH
        )
        assertEquals("pl", onlyPolish?.locale)
    }
}
