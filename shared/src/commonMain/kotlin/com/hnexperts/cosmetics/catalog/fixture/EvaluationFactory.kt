package com.hnexperts.cosmetics.catalog.fixture

import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher

object EvaluationFactory {
    fun create(): EvaluateFormula {
        return create(FixtureCatalog.ingredients)
    }

    fun create(ingredients: List<FixtureIngredient>): EvaluateFormula {
        val catalogIngredients: List<Ingredient> = ingredients.map { item -> item.ingredient }
        val matcher: IngredientMatcher = IngredientMatcher(
            ingredients = catalogIngredients,
            aliases = aliasMap(ingredients),
            commaExceptions = ingredients
                .filter { item -> item.commaException }
                .map { item -> item.ingredient.inciName }
        )
        val hazards: Map<String, IngredientHazard> = ingredients.associate { item ->
            item.ingredient.id to item.hazard
        }
        val comments: Map<String, List<LocalizedText>> = ingredients.associate { item ->
            item.ingredient.id to item.comments
        }
        return EvaluateFormula(
            matcher = matcher,
            ingredientsById = catalogIngredients.associateBy { ingredient -> ingredient.id },
            hazardsById = hazards,
            commentsById = comments,
            policy = HazardPolicy(),
            rulesetVersion = FixtureCatalog.RULESET_VERSION
        )
    }

    private fun aliasMap(ingredients: List<FixtureIngredient>): Map<String, String> {
        return ingredients.flatMap { item ->
            item.aliases.map { alias -> alias to item.ingredient.id }
        }.toMap()
    }
}
