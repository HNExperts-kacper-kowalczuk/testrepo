package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher

class CatalogIndex(
    val meta: CatalogMeta,
    val matcher: IngredientMatcher,
    val evaluateFormula: EvaluateFormula,
    val ingredientsById: Map<String, Ingredient>,
    val ingredientsSorted: List<Ingredient>,
    val commentsById: Map<String, List<LocalizedText>>,
    val hazardsById: Map<String, IngredientHazard>,
    val aliases: Map<String, String>
) {
    fun searchIngredients(query: String): List<Ingredient> {
        val needle: String = query.trim().lowercase()
        if (needle.isEmpty()) {
            return emptyList()
        }
        val aliasIds: Set<String> = aliases
            .filter { entry -> entry.key.lowercase().contains(needle) }
            .values
            .toSet()
        return ingredientsSorted
            .filter { ingredient -> matchesIngredient(ingredient, needle, aliasIds) }
            .take(INGREDIENT_SEARCH_LIMIT)
    }

    private fun matchesIngredient(
        ingredient: Ingredient,
        needle: String,
        aliasIds: Set<String>
    ): Boolean {
        if (ingredient.inciName.lowercase().contains(needle)) {
            return true
        }
        if (aliasIds.contains(ingredient.id)) {
            return true
        }
        val cas: String = ingredient.casNumbers ?: return false
        return cas.lowercase().contains(needle)
    }

    companion object {
        const val INGREDIENT_SEARCH_LIMIT: Int = 50

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
                commentsById = snapshot.comments,
                hazardsById = snapshot.hazards,
                aliases = snapshot.aliases
            )
        }
    }
}
