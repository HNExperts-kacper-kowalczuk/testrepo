package com.hnexperts.cosmetics.catalog.fixture

import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient

data class FixtureIngredient(
    val ingredient: Ingredient,
    val aliases: List<String>,
    val commaException: Boolean,
    val hazard: IngredientHazard,
    val comments: List<LocalizedText>
)

data class FixtureProduct(
    val product: Product,
    val gtins: List<String>
)
