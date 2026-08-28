package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MaintainedCatalogTagsTest {
    @Test
    fun polyethyleneGainsMicroplasticWithoutDroppingAnnex() {
        val merged: List<String> = MaintainedCatalogTags.merge(
            inciName = "POLYETHYLENE",
            existing = listOf("ANNEX_II")
        )
        assertEquals(listOf("ANNEX_II", MicroplasticIndex.TAG), merged)
    }

    @Test
    fun geraniolAndAcetylCedreneGainAllergenTags() {
        assertTrue(MaintainedCatalogTags.merge("GERANIOL", emptyList()).contains("ALLERGEN_26"))
        assertTrue(MaintainedCatalogTags.merge("ACETYL CEDRENE", emptyList()).contains("ALLERGEN_80"))
    }

    @Test
    fun glycerinStaysUntagged() {
        assertEquals(emptyList(), MaintainedCatalogTags.merge("Glycerin", emptyList()))
        assertEquals(listOf("ANNEX_III"), MaintainedCatalogTags.merge("Glycerin", listOf("ANNEX_III")))
    }

    @Test
    fun applyToUnionsTagsOnMatchingHazardRows() {
        val snapshot: CatalogSnapshot = CatalogSnapshot(
            meta = CatalogIntegrity.fixtureMeta(),
            ingredients = listOf(
                Ingredient(
                    id = "polyethylene-cosing",
                    inciName = "POLYETHYLENE",
                    casNumbers = null,
                    functionTags = emptyList()
                )
            ),
            aliases = emptyMap(),
            commaExceptions = emptyList(),
            hazards = mapOf(
                "polyethylene-cosing" to IngredientHazard(
                    ingredientId = "polyethylene-cosing",
                    dangerLevel = DangerLevel.LOW,
                    regulatoryTags = emptyList(),
                    restrictionJson = null
                )
            ),
            comments = emptyMap()
        )
        val tagged: CatalogSnapshot = MaintainedCatalogTags.applyTo(snapshot)
        val tags: List<String> = tagged.hazards.getValue("polyethylene-cosing").regulatoryTags
        assertEquals(listOf(MicroplasticIndex.TAG), tags)
        assertEquals(DangerLevel.LOW, tagged.hazards.getValue("polyethylene-cosing").dangerLevel)
    }
}
