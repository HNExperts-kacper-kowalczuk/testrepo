package com.hnexperts.cosmetics.preferences.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesExportTextTest {
    @Test
    fun avoidListUsesSortedInciNames() {
        val ingredients: Map<String, Ingredient> = mapOf(
            "parfum" to ingredient("parfum", "Parfum"),
            "glycerin" to ingredient("glycerin", "Glycerin")
        )
        val text: String = PreferencesExportText.avoidList(
            avoidedIngredientIds = setOf("parfum", "glycerin"),
            ingredientsById = ingredients,
            emptyText = "(empty)"
        )
        assertEquals("Glycerin\nParfum", text)
    }

    @Test
    fun avoidListFallsBackToEmptyText() {
        val text: String = PreferencesExportText.avoidList(
            avoidedIngredientIds = emptySet(),
            ingredientsById = emptyMap(),
            emptyText = "(empty)"
        )
        assertEquals("(empty)", text)
    }

    @Test
    fun shelfFormatsNameGtinRatingAndDate() {
        val item: ShelfItem = ShelfItem(
            shelfKey = "gtin:590",
            productId = null,
            gtin = "5901234123457",
            name = "Cleanser",
            brand = null,
            inciRaw = "Aqua",
            rating = "LOW",
            usage = ProductUsage.RINSE_OFF,
            savedAt = "2026-01-01T12:30:00Z"
        )
        val text: String = PreferencesExportText.shelf(listOf(item), emptyText = "(empty)")
        assertEquals("Cleanser / 5901234123457 / LOW / 2026-01-01 12:30", text)
    }

    private fun ingredient(id: String, name: String): Ingredient {
        return Ingredient(
            id = id,
            inciName = name,
            casNumbers = null,
            functionTags = emptyList()
        )
    }
}
