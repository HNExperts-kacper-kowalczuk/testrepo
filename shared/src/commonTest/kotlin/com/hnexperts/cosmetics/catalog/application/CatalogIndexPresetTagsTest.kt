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

class CatalogIndexPresetTagsTest {
    private val index: CatalogIndex = CatalogIndex.assemble(untaggedSnapshot())

    @Test
    fun salicylicSunCautionDoesNotChangeOverall() {
        val assessment: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, SALICYLIC ACID",
            profile = UserAvoidanceProfile.EMPTY
        )
        val finding = assessment.findings.first { row -> row.ingredient.id == "salicylic-cosing" }
        assertTrue(finding.sunCaution())
        assertEquals(DangerLevel.RESTRICTED, assessment.overall)
        assertEquals(DangerLevel.RESTRICTED, finding.level)
    }

    @Test
    fun childrenPresetUsesAssembledSalicylicTag() {
        val withPreset: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(childrenCaution = true)
        val on: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, SALICYLIC ACID",
            profile = withPreset
        )
        val off: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, SALICYLIC ACID",
            profile = UserAvoidanceProfile.EMPTY
        )
        assertTrue(on.findings.first { row -> row.ingredient.id == "salicylic-cosing" }.personalAvoid)
        assertFalse(off.findings.first { row -> row.ingredient.id == "salicylic-cosing" }.personalAvoid)
        assertEquals(off.overall, on.overall)
        assertFalse(on.suitableForUser)
        assertTrue(off.suitableForUser)
    }

    @Test
    fun pregnancyPresetUsesAssembledRetinalTag() {
        val withPreset: UserAvoidanceProfile = UserAvoidanceProfile.EMPTY.copy(pregnancyCaution = true)
        val on: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, RETINAL",
            profile = withPreset
        )
        val off: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, RETINAL",
            profile = UserAvoidanceProfile.EMPTY
        )
        assertTrue(on.findings.first { row -> row.ingredient.id == "retinal-cosing" }.personalAvoid)
        assertFalse(off.findings.first { row -> row.ingredient.id == "retinal-cosing" }.personalAvoid)
        assertEquals(DangerLevel.LOW, on.overall)
        assertFalse(on.suitableForUser)
        assertTrue(off.suitableForUser)
    }

    @Test
    fun bergamotPeelOilGetsSunCaution() {
        val assessment: ProductAssessment = index.evaluateFormula.evaluate(
            inciRaw = "Aqua, CITRUS AURANTIUM BERGAMIA PEEL OIL",
            profile = UserAvoidanceProfile.EMPTY
        )
        val finding = assessment.findings.first { row -> row.ingredient.id == "bergamot-cosing" }
        assertTrue(finding.sunCaution())
        assertEquals(DangerLevel.LOW, assessment.overall)
    }

    private fun untaggedSnapshot(): CatalogSnapshot {
        return CatalogSnapshot(
            meta = CatalogIntegrity.fixtureMeta(),
            ingredients = listOf(
                named("aqua", "Aqua"),
                named("salicylic-cosing", "SALICYLIC ACID"),
                named("retinal-cosing", "RETINAL"),
                named("bergamot-cosing", "CITRUS AURANTIUM BERGAMIA PEEL OIL")
            ),
            aliases = emptyMap(),
            commaExceptions = emptyList(),
            hazards = mapOf(
                "aqua" to hazard("aqua", DangerLevel.LOW),
                "salicylic-cosing" to hazard("salicylic-cosing", DangerLevel.RESTRICTED),
                "retinal-cosing" to hazard("retinal-cosing", DangerLevel.LOW),
                "bergamot-cosing" to hazard("bergamot-cosing", DangerLevel.LOW)
            ),
            comments = emptyMap()
        )
    }

    private fun named(id: String, inciName: String): Ingredient {
        return Ingredient(id = id, inciName = inciName, casNumbers = null, functionTags = emptyList())
    }

    private fun hazard(id: String, level: DangerLevel): IngredientHazard {
        return IngredientHazard(
            ingredientId = id,
            dangerLevel = level,
            regulatoryTags = emptyList(),
            restrictionJson = null
        )
    }
}
