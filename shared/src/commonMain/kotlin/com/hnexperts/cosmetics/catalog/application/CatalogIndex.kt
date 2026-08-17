package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.evaluation.application.EvaluateFormula
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.HazardPolicy
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher
import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

class CatalogIndex(
    val matcher: IngredientMatcher,
    val evaluateFormula: EvaluateFormula,
    val ingredientsById: Map<String, Ingredient>,
    val commentsById: Map<String, List<LocalizedText>>
) {
    companion object {
        fun load(database: CatalogDatabase): CatalogIndex {
            val ingredientRows = database.catalogDatabaseQueries.selectAllIngredients().executeAsList()
            val ingredients: List<Ingredient> = ingredientRows.map { row ->
                Ingredient(
                    id = row.id,
                    inciName = row.inci_name,
                    casNumbers = row.cas_numbers,
                    functionTags = splitTags(row.function_tags)
                )
            }
            val aliases: Map<String, String> = database.catalogDatabaseQueries.selectAllAliases().executeAsList()
                .associate { row -> row.alias_normalized to row.ingredient_id }
            val exceptions: List<String> = database.catalogDatabaseQueries.selectAllCommaExceptions().executeAsList()
                .map { row -> row.phrase_normalized }
            val hazards: Map<String, IngredientHazard> =
                database.catalogDatabaseQueries.selectAllHazards().executeAsList().associate { row ->
                    row.ingredient_id to IngredientHazard(
                        ingredientId = row.ingredient_id,
                        dangerLevel = DangerLevel.valueOf(row.danger_level),
                        regulatoryTags = splitTags(row.regulatory_tags),
                        restrictionJson = row.restriction_json
                    )
                }
            val comments: Map<String, List<LocalizedText>> =
                database.catalogDatabaseQueries.selectAllComments().executeAsList()
                    .groupBy { row -> row.ingredient_id }
                    .mapValues { entry ->
                        entry.value.map { row ->
                            LocalizedText(locale = row.locale, summary = row.summary, detail = row.detail)
                        }
                    }
            val matcher: IngredientMatcher = IngredientMatcher(ingredients, aliases, exceptions)
            val evaluateFormula: EvaluateFormula = EvaluateFormula(
                matcher = matcher,
                ingredientsById = ingredients.associateBy { ingredient -> ingredient.id },
                hazardsById = hazards,
                commentsById = comments,
                policy = HazardPolicy(),
                rulesetVersion = FixtureCatalog.RULESET_VERSION
            )
            return CatalogIndex(
                matcher = matcher,
                evaluateFormula = evaluateFormula,
                ingredientsById = ingredients.associateBy { ingredient -> ingredient.id },
                commentsById = comments
            )
        }

        private fun splitTags(raw: String?): List<String> {
            if (raw.isNullOrBlank()) {
                return emptyList()
            }
            return raw.split(',').map { tag -> InciNormalizer.normalize(tag) }.filter { tag -> tag.isNotEmpty() }
        }
    }
}
