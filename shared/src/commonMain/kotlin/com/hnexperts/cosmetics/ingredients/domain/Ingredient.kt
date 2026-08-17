package com.hnexperts.cosmetics.ingredients.domain

data class Ingredient(
    val id: String,
    val inciName: String,
    val casNumbers: String?,
    val functionTags: List<String>
)
