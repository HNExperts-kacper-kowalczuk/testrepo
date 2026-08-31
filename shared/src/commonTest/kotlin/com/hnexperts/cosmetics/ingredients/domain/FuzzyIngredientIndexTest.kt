package com.hnexperts.cosmetics.ingredients.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FuzzyIngredientIndexTest {
    @Test
    fun uniqueTypoIsDistanceOneAndUnique() {
        val index: FuzzyIngredientIndex = FuzzyIngredientIndex(
            ingredients = listOf(named("left", "AAAAAAAA"), named("right", "CCCCCCCC")),
            aliasToIngredient = emptyMap()
        )
        val hit: FuzzyHit? = index.findHit("AAAAAAAB")
        assertNotNull(hit)
        assertEquals("left", hit.ingredient.id)
        assertEquals(1, hit.distance)
        assertTrue(hit.unique)
    }

    @Test
    fun twoCloseNamesAreNotUnique() {
        val index: FuzzyIngredientIndex = FuzzyIngredientIndex(
            ingredients = listOf(named("left", "AAAAAAAA"), named("right", "AAAAAAAB")),
            aliasToIngredient = emptyMap()
        )
        val hit: FuzzyHit? = index.findHit("AAAAAAAC")
        assertNotNull(hit)
        assertEquals(1, hit.distance)
        assertFalse(hit.unique)
    }

    @Test
    fun aliasOfSameIngredientDoesNotCountAsRunnerUp() {
        val ingredient: Ingredient = named("only", "Niacinamide")
        val index: FuzzyIngredientIndex = FuzzyIngredientIndex(
            ingredients = listOf(ingredient),
            aliasToIngredient = mapOf("NYACINAMIDE" to ingredient)
        )
        val hit: FuzzyHit? = index.findHit("NIACINAM1DE")
        assertNotNull(hit)
        assertEquals("only", hit.ingredient.id)
        assertEquals(1, hit.distance)
        assertTrue(hit.unique)
    }

    @Test
    fun shortUnknownIsNotAHit() {
        val index: FuzzyIngredientIndex = FuzzyIngredientIndex(
            ingredients = listOf(named("aqua", "Aqua")),
            aliasToIngredient = emptyMap()
        )
        assertNull(index.findHit("XYZ"))
    }

    private fun named(id: String, inciName: String): Ingredient {
        return Ingredient(id = id, inciName = inciName, casNumbers = null, functionTags = emptyList())
    }
}
