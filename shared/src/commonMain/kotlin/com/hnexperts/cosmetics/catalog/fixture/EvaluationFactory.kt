package com.hnexperts.cosmetics.catalog.fixture

import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher

object EvaluationFactory {
    fun create(): EvaluateFormula {
        val ingredients: List<Ingredient> = FixtureCatalog.ingredients.map { item -> item.ingredient }
        val matcher: IngredientMatcher = IngredientMatcher(
            ingredients = ingredients,
            aliases = FixtureCatalog.aliasMap(),
            commaExceptions = FixtureCatalog.commaExceptions()
        )
        val hazards: Map<String, IngredientHazard> = FixtureCatalog.ingredients.associate { item ->
            item.ingredient.id to item.hazard
        }
        val comments: Map<String, List<LocalizedText>> = FixtureCatalog.ingredients.associate { item ->
            item.ingredient.id to item.comments
        }
        return EvaluateFormula(
            matcher = matcher,
            ingredientsById = ingredients.associateBy { ingredient -> ingredient.id },
            hazardsById = hazards,
            commentsById = comments,
            policy = HazardPolicy(),
            rulesetVersion = FixtureCatalog.RULESET_VERSION
        )
    }
}
