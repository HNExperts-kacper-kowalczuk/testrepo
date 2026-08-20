package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EuAllergenIngestTest {
    @Test
    fun ingestedVanillinIsPersonalAvoidWhenPresetOn() {
        val ingested = CosingIngredientDump(
            region = "EU",
            catalogVersion = "ingest",
            rulesetVersion = "ingest",
            builtAt = "2026-01-01T00:00:00Z",
            ingredients = listOf(
                CosingIngredientRecord(
                    id = "vanillin",
                    inciName = "Vanillin",
                    dangerLevel = "LOW",
                    regulatoryTags = EuLabelledAllergenIndex.tagsFor("Vanillin"),
                    functionTags = listOf("PERFUMING"),
                    comments = emptyList()
                )
            )
        )
        val build: CatalogBuild = ShippedCatalogMerger.merge(
            ingestedIngredients = ingested,
            ingestedProducts = ObfProductDump(region = "EU", products = emptyList()),
            builtAt = "2026-08-20T00:00:00Z"
        )
        val evaluate = EvaluationFactory.create(build.ingredients)
        val withPreset: ProductAssessment = evaluate.evaluate(
            inciRaw = "Aqua, Vanillin",
            profile = UserAvoidanceProfile.EMPTY.copy(euAllergens = true)
        )
        val withoutPreset: ProductAssessment = evaluate.evaluate(
            inciRaw = "Aqua, Vanillin",
            profile = UserAvoidanceProfile.EMPTY
        )
        val vanillinOn = withPreset.findings.first { finding -> finding.ingredient.id == "vanillin" }
        val vanillinOff = withoutPreset.findings.first { finding -> finding.ingredient.id == "vanillin" }
        assertTrue(vanillinOn.personalAvoid)
        assertFalse(vanillinOff.personalAvoid)
        assertEquals(DangerLevel.LOW, withPreset.overall)
        assertEquals(withoutPreset.overall, withPreset.overall)
        assertFalse(withPreset.suitableForUser)
        assertTrue(withoutPreset.suitableForUser)
    }
}
