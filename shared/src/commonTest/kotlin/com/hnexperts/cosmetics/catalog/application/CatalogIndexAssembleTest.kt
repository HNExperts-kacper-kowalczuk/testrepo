package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogIndexAssembleTest {
    @Test
    fun assembleSortsIngredientsAndWiresMatcher() {
        val snapshot: CatalogSnapshot = CatalogSnapshot(
            ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
            aliases = FixtureCatalog.aliasMap(),
            commaExceptions = FixtureCatalog.commaExceptions(),
            hazards = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.hazard },
            comments = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.comments }
        )
        val index: CatalogIndex = CatalogIndex.assemble(snapshot)
        assertEquals(snapshot.ingredients.size, index.ingredientsSorted.size)
        assertEquals(
            snapshot.ingredients.map { ingredient -> ingredient.inciName }.sorted(),
            index.ingredientsSorted.map { ingredient -> ingredient.inciName }
        )
        assertEquals("aqua", index.matcher.matchToken("Water").id)
        val assessmentUnknowns: Int = index.evaluateFormula.evaluate("Aqua, Glycerin", UserAvoidanceProfile.EMPTY).unknownCount
        assertTrue(assessmentUnknowns == 0)
    }
}
