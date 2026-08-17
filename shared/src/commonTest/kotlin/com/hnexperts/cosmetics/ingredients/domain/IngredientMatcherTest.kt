package com.hnexperts.cosmetics.ingredients.domain

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientMatcherTest {
    private val matcher: IngredientMatcher = IngredientMatcher(
        ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
        aliases = FixtureCatalog.aliasMap(),
        commaExceptions = FixtureCatalog.commaExceptions()
    )

    @Test
    fun splitsOnCommasButKeepsHexanediolTogether() {
        val refs: List<IngredientRef> = matcher.matchList("Aqua, 1,2-Hexanediol, Glycerin")
        assertEquals(3, refs.size)
        assertEquals("aqua", refs[0].id)
        assertEquals("hexanediol", refs[1].id)
        assertEquals(MatchMethod.EXACT, refs[1].matchedBy)
        assertEquals("glycerin", refs[2].id)
    }

    @Test
    fun matchesAquaWaterAliasAndSlashForm() {
        val water: IngredientRef = matcher.matchToken("Water")
        assertEquals("aqua", water.id)
        assertEquals(MatchMethod.ALIAS, water.matchedBy)

        val slash: IngredientRef = matcher.matchToken("Aqua/Water")
        assertEquals("aqua", slash.id)
    }

    @Test
    fun matchesParfumInParentheses() {
        val ref: IngredientRef = matcher.matchToken("Parfum (Fragrance)")
        assertEquals("parfum", ref.id)
    }

    @Test
    fun matchesCiNumbers() {
        val yellow: IngredientRef = matcher.matchToken("CI 19140")
        assertEquals("ci-19140", yellow.id)
        val titanium: IngredientRef = matcher.matchToken("CI 77891")
        assertEquals("titanium-dioxide", titanium.id)
    }

    @Test
    fun fuzzyMatchesOcrTypo() {
        val ref: IngredientRef = matcher.matchToken("NIACINAM1DE")
        assertEquals("niacinamide", ref.id)
        assertEquals(MatchMethod.FUZZY, ref.matchedBy)
    }

    @Test
    fun doesNotFuzzyMatchShortUnknownTokens() {
        val ref: IngredientRef = matcher.matchToken("XYZ")
        assertEquals(MatchMethod.UNMATCHED, ref.matchedBy)
        assertEquals(null, ref.id)
    }

    @Test
    fun unmatchedTokenKeepsDisplayName() {
        val ref: IngredientRef = matcher.matchToken("CompletelyUnknownStuff")
        assertEquals(MatchMethod.UNMATCHED, ref.matchedBy)
        assertTrue(ref.displayName.contains("Unknown", ignoreCase = true))
    }
}
