package com.hnexperts.cosmetics.hazards.domain

data class LocalizedText(
    val locale: String,
    val summary: String,
    val detail: String?
)

data class IngredientHazard(
    val ingredientId: String,
    val dangerLevel: DangerLevel,
    val regulatoryTags: List<String>,
    val restrictionJson: String?
)
