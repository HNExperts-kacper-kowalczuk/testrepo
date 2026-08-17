package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient

data class CatalogSnapshot(
    val meta: CatalogMeta,
    val ingredients: List<Ingredient>,
    val aliases: Map<String, String>,
    val commaExceptions: List<String>,
    val hazards: Map<String, IngredientHazard>,
    val comments: Map<String, List<LocalizedText>>
) {
    val rulesetVersion: String
        get() = meta.rulesetVersion
}
