package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher

class CatalogIndex(
    val meta: CatalogMeta,
    val matcher: IngredientMatcher,
    val evaluateFormula: EvaluateFormula,
    val ingredientsById: Map<String, Ingredient>,
    val ingredientsSorted: List<Ingredient>,
    val commentsById: Map<String, List<LocalizedText>>
) {
    companion object {
        fun assemble(snapshot: CatalogSnapshot): CatalogIndex {
            val matcher: IngredientMatcher = IngredientMatcher(
                ingredients = snapshot.ingredients,
                aliases = snapshot.aliases,
                commaExceptions = snapshot.commaExceptions
            )
            val ingredientsById: Map<String, Ingredient> =
                snapshot.ingredients.associateBy { ingredient -> ingredient.id }
            val evaluateFormula: EvaluateFormula = EvaluateFormula(
                matcher = matcher,
                ingredientsById = ingredientsById,
                hazardsById = snapshot.hazards,
                commentsById = snapshot.comments,
                policy = HazardPolicy(),
                rulesetVersion = snapshot.rulesetVersion
            )
            return CatalogIndex(
                meta = snapshot.meta,
                matcher = matcher,
                evaluateFormula = evaluateFormula,
                ingredientsById = ingredientsById,
                ingredientsSorted = snapshot.ingredients.sortedBy { ingredient -> ingredient.inciName },
                commentsById = snapshot.comments
            )
        }
    }
}
