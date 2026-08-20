package com.hnexperts.cosmetics.preferences.application

import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.shelf.domain.ShelfItem

object PreferencesExportText {
    fun avoidList(
        avoidedIngredientIds: Set<String>,
        ingredientsById: Map<String, Ingredient>,
        emptyText: String
    ): String {
        val names: List<String> = avoidedIngredientIds
            .map { ingredientId -> ingredientsById[ingredientId]?.inciName ?: ingredientId }
            .sorted()
        return names.joinToString(separator = "\n").ifBlank { emptyText }
    }

    fun shelf(items: List<ShelfItem>, emptyText: String): String {
        if (items.isEmpty()) {
            return emptyText
        }
        return items.joinToString(separator = "\n") { item ->
            val name: String = item.name.orEmpty()
            val gtin: String = item.gtin.orEmpty()
            val date: String = item.savedAt.replace('T', ' ').take(16)
            "$name / $gtin / ${item.rating} / $date"
        }
    }
}
