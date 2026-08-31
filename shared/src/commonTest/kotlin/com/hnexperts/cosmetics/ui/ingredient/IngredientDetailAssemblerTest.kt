package com.hnexperts.cosmetics.ui.ingredient

import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IngredientDetailAssemblerTest {
    private val index: CatalogIndex = fixtureIndex()
    private val localizer: CommentLocalizer = CommentLocalizer()

    @Test
    fun aquaViaAliasIncludesSynonymsAndFunction() {
        val finding: Finding = findingFor("Water")
        val detail: IngredientDetail = IngredientDetailAssembler.fromFinding(
            finding = finding,
            index = index,
            comment = localizer.pick(finding.comments, AppLocale.ENGLISH)
        )
        assertEquals("Aqua", detail.title)
        assertEquals(MatchMethod.ALIAS, detail.matchMethod)
        assertEquals(listOf("Eau", "Water"), detail.aliases)
        assertEquals(listOf("SOLVENT"), detail.functionTags)
        assertEquals("Water. No relevant restriction in this ruleset.", detail.summary)
        assertNull(detail.detail)
        assertNull(detail.catalogInciName)
        assertEquals(1, detail.listPosition)
        assertFalse(detail.unmatched)
        assertEquals(DangerLevel.SAFE, detail.level)
    }

    @Test
    fun alcoholDenatIncludesUsageRestriction() {
        val finding: Finding = findingFor("Alcohol Denat.")
        val detail: IngredientDetail = IngredientDetailAssembler.fromFinding(
            finding = finding,
            index = index,
            comment = localizer.pick(finding.comments, AppLocale.ENGLISH)
        )
        assertEquals("Alcohol Denat.", detail.title)
        assertEquals(MatchMethod.EXACT, detail.matchMethod)
        val restriction = detail.restriction
        assertNotNull(restriction)
        assertEquals("MODERATE", restriction.leaveOn)
        assertEquals("LOW", restriction.rinseOff)
        assertTrue(detail.aliases.contains("Ethanol"))
    }

    @Test
    fun unmatchedBlobHasNoCatalogExtras() {
        val finding: Finding = findingFor("TotallyFakeIngredient")
        val detail: IngredientDetail = IngredientDetailAssembler.fromFinding(
            finding = finding,
            index = index,
            comment = localizer.pick(finding.comments, AppLocale.ENGLISH)
        )
        assertEquals("TOTALLYFAKEINGREDIENT", detail.title)
        assertTrue(detail.unmatched)
        assertEquals(MatchMethod.UNMATCHED, detail.matchMethod)
        assertTrue(detail.aliases.isEmpty())
        assertTrue(detail.functionTags.isEmpty())
        assertTrue(detail.regulatoryTags.isEmpty())
        assertNull(detail.casNumbers)
        assertNull(detail.restriction)
        assertNull(detail.summary)
        assertEquals(1, detail.listPosition)
    }

    @Test
    fun commentDetailIsKeptWhenPresent() {
        val finding: Finding = findingFor("Aqua")
        val comment: LocalizedText = LocalizedText(
            locale = "en",
            summary = "Short note.",
            detail = "Longer catalog explanation for this ingredient."
        )
        val detail: IngredientDetail = IngredientDetailAssembler.fromFinding(finding, index, comment)
        assertEquals("Short note.", detail.summary)
        assertEquals("Longer catalog explanation for this ingredient.", detail.detail)
    }

    @Test
    fun catalogSearchIncludesFunctionsAndAnnexTag() {
        val ingredient = index.ingredientsById.getValue("phenoxyethanol")
        val comments: List<LocalizedText> = index.commentsById[ingredient.id].orEmpty()
        val detail: IngredientDetail = IngredientDetailAssembler.fromCatalogIngredient(
            ingredient = ingredient,
            index = index,
            comment = localizer.pick(comments, AppLocale.ENGLISH),
            level = index.hazardsById.getValue(ingredient.id).dangerLevel
        )
        assertEquals("Phenoxyethanol", detail.title)
        assertTrue(detail.functionTags.contains("PRESERVATIVE"))
        assertTrue(detail.regulatoryTags.contains("ANNEX_V"))
        assertNull(detail.listPosition)
        assertNull(detail.matchMethod)
        assertFalse(detail.unmatched)
        assertFalse(detail.earlyList)
    }

    @Test
    fun phototoxicFindingSetsSunCaution() {
        val finding: Finding = findingFor("Salicylic Acid")
        val detail: IngredientDetail = IngredientDetailAssembler.fromFinding(
            finding = finding,
            index = index,
            comment = localizer.pick(finding.comments, AppLocale.ENGLISH)
        )
        assertTrue(detail.sunCaution)
        assertTrue(detail.regulatoryTags.contains("PHOTOTOXIC"))
        assertTrue(detail.earlyList)
    }

    private fun findingFor(inciRaw: String): Finding {
        return index.evaluateFormula.evaluate(inciRaw, UserAvoidanceProfile.EMPTY).findings.first()
    }

    private fun fixtureIndex(): CatalogIndex {
        return CatalogIndex.assemble(
            CatalogSnapshot(
                meta = CatalogIntegrity.fixtureMeta(),
                ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
                aliases = FixtureCatalog.aliasMap(),
                commaExceptions = FixtureCatalog.commaExceptions(),
                hazards = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.hazard },
                comments = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.comments }
            )
        )
    }
}
