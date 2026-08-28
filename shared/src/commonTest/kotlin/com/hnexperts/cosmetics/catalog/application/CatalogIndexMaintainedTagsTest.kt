package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogIndexMaintainedTagsTest {
    private val index: CatalogIndex = CatalogIndex.assemble(untaggedCosingSnapshot())

    @Test
    fun polyethyleneChipDoesNotChangeOverall() {
        val assessment: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, POLYETHYLENE",
            profile = UserAvoidanceProfile.EMPTY
        )
        val finding = assessment.findings.first { row -> row.ingredient.id == "polyethylene-cosing" }
        assertTrue(finding.microplastic())
        assertTrue(assessment.hasMicroplastics())
        assertEquals(DangerLevel.LOW, assessment.overall)
        assertEquals(DangerLevel.LOW, finding.level)
    }

    @Test
    fun carmineChipDoesNotChangeOverall() {
        val assessment: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, CARMINE",
            profile = UserAvoidanceProfile.EMPTY
        )
        val finding = assessment.findings.first { row -> row.ingredient.id == "carmine-cosing" }
        assertTrue(finding.animalDerived())
        assertTrue(assessment.hasAnimalDerived())
        assertEquals(DangerLevel.LOW, assessment.overall)
        assertEquals(DangerLevel.LOW, finding.level)
        assertFalse(assessment.hasMicroplastics())
    }

    @Test
    fun euAllergensPresetUsesAssembledTags() {
        val withPreset: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(euAllergens = true)
        val geraniolOn: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, GERANIOL",
            profile = withPreset
        )
        val geraniolOff: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, GERANIOL",
            profile = UserAvoidanceProfile.EMPTY
        )
        val cedreneOn: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, ACETYL CEDRENE",
            profile = withPreset
        )
        assertTrue(geraniolOn.findings.first { row -> row.ingredient.id == "geraniol-cosing" }.personalAvoid)
        assertFalse(geraniolOff.findings.first { row -> row.ingredient.id == "geraniol-cosing" }.personalAvoid)
        assertTrue(cedreneOn.findings.first { row -> row.ingredient.id == "acetyl-cedrene-cosing" }.personalAvoid)
        assertEquals(DangerLevel.LOW, geraniolOn.overall)
        assertEquals(geraniolOff.overall, geraniolOn.overall)
        assertFalse(geraniolOn.suitableForUser)
        assertTrue(geraniolOff.suitableForUser)
    }

    private fun untaggedCosingSnapshot(): CatalogSnapshot {
        return CatalogSnapshot(
            meta = CatalogIntegrity.fixtureMeta(),
            ingredients = listOf(
                named("aqua", "Aqua"),
                named("polyethylene-cosing", "POLYETHYLENE"),
                named("carmine-cosing", "CARMINE"),
                named("geraniol-cosing", "GERANIOL"),
                named("acetyl-cedrene-cosing", "ACETYL CEDRENE")
            ),
            aliases = emptyMap(),
            commaExceptions = emptyList(),
            hazards = mapOf(
                "aqua" to lowHazard("aqua"),
                "polyethylene-cosing" to lowHazard("polyethylene-cosing"),
                "carmine-cosing" to lowHazard("carmine-cosing"),
                "geraniol-cosing" to lowHazard("geraniol-cosing"),
                "acetyl-cedrene-cosing" to lowHazard("acetyl-cedrene-cosing")
            ),
            comments = emptyMap()
        )
    }

    private fun named(id: String, inciName: String): Ingredient {
        return Ingredient(id = id, inciName = inciName, casNumbers = null, functionTags = emptyList())
    }

    private fun lowHazard(id: String): IngredientHazard {
        return IngredientHazard(
            ingredientId = id,
            dangerLevel = DangerLevel.LOW,
            regulatoryTags = emptyList(),
            restrictionJson = null
        )
    }
}
