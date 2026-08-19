package com.hnexperts.cosmetics.evaluation.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef

data class Finding(
    val ingredient: IngredientRef,
    val level: DangerLevel,
    val regulatoryTags: List<String>,
    val comments: List<LocalizedText>,
    val personalAvoid: Boolean,
    val usageAdjusted: Boolean = false
)

data class ProductAssessment(
    val productName: String?,
    val brand: String?,
    val gtin: String?,
    val inciRaw: String,
    val overall: DangerLevel,
    val suitableForUser: Boolean,
    val findings: List<Finding>,
    val unknownCount: Int,
    val rulesetVersion: String,
    val usage: ProductUsage = ProductUsage.UNKNOWN,
    val usageAssumed: Boolean = false,
    val packVerified: Boolean = false
)
