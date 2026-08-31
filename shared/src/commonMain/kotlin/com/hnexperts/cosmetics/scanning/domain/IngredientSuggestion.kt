package com.hnexperts.cosmetics.scanning.domain

data class IngredientSuggestion(
    val id: String,
    val inciName: String,
    val distance: Int?
)

data class ReviewSuggestionLists(
    val nearby: List<IngredientSuggestion>,
    val search: List<IngredientSuggestion>
)

object ReviewMatchSuggestions {
    const val NEARBY_LIMIT: Int = 5

    fun merge(
        nearby: List<IngredientSuggestion>,
        search: List<IngredientSuggestion>
    ): ReviewSuggestionLists {
        val nearbyIds: Set<String> = nearby.map { suggestion -> suggestion.id }.toSet()
        return ReviewSuggestionLists(
            nearby = nearby,
            search = search.filter { suggestion -> !nearbyIds.contains(suggestion.id) }
        )
    }
}
