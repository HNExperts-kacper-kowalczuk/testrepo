package com.hnexperts.cosmetics.catalog.data

import com.hnexperts.cosmetics.catalog.application.CatalogSnapshot
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

class CatalogSnapshotReader(
    private val database: CatalogDatabase
) {
    fun read(): CatalogSnapshot {
        val meta = database.catalogDatabaseQueries.selectMeta().executeAsOneOrNull()
            ?: throw IllegalStateException("catalog_meta is missing after seed")
        return CatalogSnapshot(
            rulesetVersion = meta.ruleset_version,
            ingredients = readIngredients(),
            aliases = readAliases(),
            commaExceptions = readCommaExceptions(),
            hazards = readHazards(),
            comments = readComments()
        )
    }

    private fun readIngredients(): List<Ingredient> {
        return database.catalogDatabaseQueries.selectAllIngredients().executeAsList().map { row ->
            Ingredient(
                id = row.id,
                inciName = row.inci_name,
                casNumbers = row.cas_numbers,
                functionTags = splitTags(row.function_tags)
            )
        }
    }

    private fun readAliases(): Map<String, String> {
        return database.catalogDatabaseQueries.selectAllAliases().executeAsList()
            .associate { row -> row.alias_normalized to row.ingredient_id }
    }

    private fun readCommaExceptions(): List<String> {
        return database.catalogDatabaseQueries.selectAllCommaExceptions().executeAsList()
            .map { row -> row.phrase_normalized }
    }

    private fun readHazards(): Map<String, IngredientHazard> {
        return database.catalogDatabaseQueries.selectAllHazards().executeAsList().associate { row ->
            row.ingredient_id to IngredientHazard(
                ingredientId = row.ingredient_id,
                dangerLevel = DangerLevelParser.parse(row.danger_level),
                regulatoryTags = splitTags(row.regulatory_tags),
                restrictionJson = row.restriction_json
            )
        }
    }

    private fun readComments(): Map<String, List<LocalizedText>> {
        return database.catalogDatabaseQueries.selectAllComments().executeAsList()
            .groupBy { row -> row.ingredient_id }
            .mapValues { entry ->
                entry.value.map { row ->
                    LocalizedText(locale = row.locale, summary = row.summary, detail = row.detail)
                }
            }
    }

    private fun splitTags(raw: String?): List<String> {
        if (raw.isNullOrBlank()) {
            return emptyList()
        }
        return raw.split(',').map { tag -> InciNormalizer.normalize(tag) }.filter { tag -> tag.isNotEmpty() }
    }
}
