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
    fun slashSynonymRequiresEveryPartToResolveToTheSameIngredient() {
        val synonym: IngredientRef = matcher.matchToken("Parfum/Fragrance")
        assertEquals("parfum", synonym.id)
        assertEquals(MatchMethod.ALIAS, synonym.matchedBy)

        val tripleSynonym: IngredientRef = matcher.matchToken("Aqua/Water/Eau")
        assertEquals("aqua", tripleSynonym.id)
    }

    @Test
    fun compoundSlashNamesMatchAsOneIngredient() {
        val triglyceride: IngredientRef = matcher.matchToken("Caprylic/Capric Triglyceride")
        assertEquals("caprylic-capric-triglyceride", triglyceride.id)
        assertEquals(MatchMethod.EXACT, triglyceride.matchedBy)

        val copolymer: IngredientRef = matcher.matchToken("Styrene/Acrylates Copolymer")
        assertEquals("styrene-acrylates-copolymer", copolymer.id)
        assertEquals(MatchMethod.EXACT, copolymer.matchedBy)
    }

    @Test
    fun slashPartsResolvingToDifferentIngredientsStayUnmatched() {
        val mixed: IngredientRef = matcher.matchToken("Aqua/Glycerin")
        assertEquals(MatchMethod.UNMATCHED, mixed.matchedBy)
        assertEquals(null, mixed.id)
    }

    @Test
    fun compoundSlashTokenIsNotMatchedByItsFirstPartAlone() {
        val unknownCompound: IngredientRef = matcher.matchToken("Aqua/Completelyunknownstuff")
        assertEquals(MatchMethod.UNMATCHED, unknownCompound.matchedBy)
        assertEquals(null, unknownCompound.id)
    }

    @Test
    fun trailingDotAbbreviationsMatchWithOrWithoutTheDot() {
        val withDot: IngredientRef = matcher.matchToken("Alcohol Denat.")
        assertEquals("alcohol-denat", withDot.id)

        val withoutDot: IngredientRef = matcher.matchToken("Alcohol Denat")
        assertEquals("alcohol-denat", withoutDot.id)
        assertEquals(MatchMethod.EXACT, withoutDot.matchedBy)

        val longForm: IngredientRef = matcher.matchToken("Alcohol Denatured")
        assertEquals("alcohol-denat", longForm.id)
    }

    @Test
    fun nanoSuffixIsStrippedForMatching() {
        val parens: IngredientRef = matcher.matchToken("Titanium Dioxide (nano)")
        assertEquals("titanium-dioxide", parens.id)

        val brackets: IngredientRef = matcher.matchToken("Titanium Dioxide [nano]")
        assertEquals("titanium-dioxide", brackets.id)
        assertEquals(MatchMethod.EXACT, brackets.matchedBy)
    }

    @Test
    fun fuzzyMatchesTypoInCompoundSlashName() {
        val ref: IngredientRef = matcher.matchToken("Caprylic/Capric Triglycerlde")
        assertEquals("caprylic-capric-triglyceride", ref.id)
        assertEquals(MatchMethod.FUZZY, ref.matchedBy)
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
