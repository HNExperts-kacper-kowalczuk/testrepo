package com.hnexperts.cosmetics.hazards.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile

class HazardPolicy {
    fun assess(
        reference: IngredientRef,
        ingredient: Ingredient?,
        hazard: IngredientHazard?,
        comments: List<LocalizedText>,
        profile: UserAvoidanceProfile,
        usage: ProductUsage = ProductUsage.UNKNOWN
    ): Finding {
        val baseline: DangerLevel = baselineLevel(reference, hazard)
        val level: DangerLevel = if (baseline == DangerLevel.UNKNOWN || hazard == null) {
            baseline
        } else {
            UsageRestrictionInterpreter.effectiveLevel(baseline, hazard.restrictionJson, usage)
        }
        val tags: List<String> = hazard?.regulatoryTags.orEmpty()
        val functionTags: List<String> = ingredient?.functionTags.orEmpty()
        val personalAvoid: Boolean = reference.id != null &&
            profile.avoids(reference.id, functionTags, tags, usage.scoringUsage())
        return Finding(
            ingredient = reference,
            level = level,
            regulatoryTags = tags,
            comments = comments,
            personalAvoid = personalAvoid,
            usageAdjusted = baseline != DangerLevel.UNKNOWN && level != baseline
        )
    }

    private fun baselineLevel(reference: IngredientRef, hazard: IngredientHazard?): DangerLevel {
        return when {
            reference.matchedBy == MatchMethod.UNMATCHED -> DangerLevel.UNKNOWN
            hazard == null -> DangerLevel.UNKNOWN
            else -> hazard.dangerLevel
        }
    }
}
