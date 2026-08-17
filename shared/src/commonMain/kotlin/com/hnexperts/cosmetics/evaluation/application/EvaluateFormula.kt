package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.DangerLevelOrdering
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

class EvaluateFormula(
    private val matcher: IngredientMatcher,
    private val ingredientsById: Map<String, Ingredient>,
    private val hazardsById: Map<String, IngredientHazard>,
    private val commentsById: Map<String, List<LocalizedText>>,
    private val policy: HazardPolicy,
    private val rulesetVersion: String
) {
    fun evaluate(
        inciRaw: String,
        profile: UserAvoidanceProfile,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null
    ): ProductAssessment {
        return toAssessment(
            references = matcher.matchList(inciRaw),
            inciRaw = inciRaw,
            profile = profile,
            productName = productName,
            brand = brand,
            gtin = gtin
        )
    }

    suspend fun evaluateAsync(
        inciRaw: String,
        profile: UserAvoidanceProfile,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null
    ): ProductAssessment {
        coroutineContext.ensureActive()
        return toAssessment(
            references = matcher.matchListConcurrently(inciRaw),
            inciRaw = inciRaw,
            profile = profile,
            productName = productName,
            brand = brand,
            gtin = gtin
        )
    }

    private fun toAssessment(
        references: List<IngredientRef>,
        inciRaw: String,
        profile: UserAvoidanceProfile,
        productName: String?,
        brand: String?,
        gtin: String?
    ): ProductAssessment {
        val findings: List<Finding> = references.map { reference ->
            val ingredient: Ingredient? = reference.id?.let { id -> ingredientsById[id] }
            val hazard: IngredientHazard? = reference.id?.let { id -> hazardsById[id] }
            val comments: List<LocalizedText> = reference.id?.let { id -> commentsById[id] }.orEmpty()
            policy.assess(reference, ingredient, hazard, comments, profile)
        }
        val unknownCount: Int = findings.count { finding ->
            finding.ingredient.matchedBy == MatchMethod.UNMATCHED || finding.level == DangerLevel.UNKNOWN
        }
        val overall: DangerLevel = DangerLevelOrdering.overall(findings.map { finding -> finding.level })
        val suitableForUser: Boolean = findings.none { finding -> finding.personalAvoid }
        return ProductAssessment(
            productName = productName,
            brand = brand,
            gtin = gtin,
            inciRaw = inciRaw,
            overall = overall,
            suitableForUser = suitableForUser,
            findings = findings,
            unknownCount = unknownCount,
            rulesetVersion = rulesetVersion
        )
    }
}
