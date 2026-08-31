package com.hnexperts.cosmetics.ui.ingredient

import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod

data class IngredientDetail(
    val title: String,
    val catalogInciName: String?,
    val casNumbers: String?,
    val level: DangerLevel?,
    val summary: String?,
    val detail: String?,
    val aliases: List<String>,
    val functionTags: List<String>,
    val regulatoryTags: List<String>,
    val restriction: UsageRestriction?,
    val matchMethod: MatchMethod?,
    val listPosition: Int?,
    val sunCaution: Boolean,
    val earlyList: Boolean,
    val usageAdjusted: Boolean,
    val personalAvoid: Boolean,
    val unmatched: Boolean
)
