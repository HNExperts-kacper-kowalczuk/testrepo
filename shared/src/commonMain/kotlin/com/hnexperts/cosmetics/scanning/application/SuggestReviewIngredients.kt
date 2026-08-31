package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.ingredients.domain.FuzzyHit
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.scanning.domain.IngredientSuggestion
import com.hnexperts.cosmetics.scanning.domain.ReviewMatchSuggestions
import com.hnexperts.cosmetics.scanning.domain.ReviewSuggestionLists
import kotlinx.coroutines.withContext

class SuggestReviewIngredients(
    private val catalog: CatalogGateway,
    private val dispatchers: AppDispatchers
) {
    suspend fun invoke(rawText: String, query: String): Outcome<ReviewSuggestionLists> {
        val indexOutcome: Outcome<CatalogIndex> = catalog.awaitIndex()
        val index: CatalogIndex = when (indexOutcome) {
            is Outcome.Err -> return indexOutcome
            is Outcome.Ok -> indexOutcome.value
        }
        return FailureCatcher.ocr("ocr.suggest") {
            withContext(dispatchers.computation) {
                listsFor(index, rawText, query)
            }
        }
    }

    private fun listsFor(
        index: CatalogIndex,
        rawText: String,
        query: String
    ): ReviewSuggestionLists {
        val nearby: List<IngredientSuggestion> = nearbySuggestions(index, rawText)
        val searchQuery: String = query.ifBlank { rawText }
        val searchHits: List<IngredientSuggestion> = searchSuggestions(index, searchQuery)
        return ReviewMatchSuggestions.merge(nearby, searchHits)
    }

    private fun nearbySuggestions(index: CatalogIndex, rawText: String): List<IngredientSuggestion> {
        if (rawText.isBlank()) {
            return emptyList()
        }
        return index.matcher.suggest(rawText, ReviewMatchSuggestions.NEARBY_LIMIT).map(::fromHit)
    }

    private fun searchSuggestions(index: CatalogIndex, query: String): List<IngredientSuggestion> {
        return index.searchIngredients(query).map(::fromIngredient)
    }

    private fun fromHit(hit: FuzzyHit): IngredientSuggestion {
        return IngredientSuggestion(
            id = hit.ingredient.id,
            inciName = hit.ingredient.inciName,
            distance = hit.distance
        )
    }

    private fun fromIngredient(ingredient: Ingredient): IngredientSuggestion {
        return IngredientSuggestion(
            id = ingredient.id,
            inciName = ingredient.inciName,
            distance = null
        )
    }
}
