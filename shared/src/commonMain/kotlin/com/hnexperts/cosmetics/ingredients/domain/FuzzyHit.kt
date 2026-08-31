package com.hnexperts.cosmetics.ingredients.domain

data class FuzzyHit(
    val ingredient: Ingredient,
    val distance: Int,
    val unique: Boolean
)

data class MatchedToken(
    val reference: IngredientRef,
    val fuzzy: FuzzyHit? = null
)
