package com.hnexperts.cosmetics.ingredients.domain

enum class MatchMethod {
    EXACT,
    ALIAS,
    FUZZY,
    UNMATCHED
}

data class IngredientRef(
    val id: String?,
    val displayName: String,
    val matchedBy: MatchMethod
)
