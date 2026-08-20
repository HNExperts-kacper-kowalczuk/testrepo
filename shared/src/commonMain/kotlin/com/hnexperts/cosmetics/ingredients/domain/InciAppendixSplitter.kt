package com.hnexperts.cosmetics.ingredients.domain

/**
 * EU packs often list labelled allergens after the main INCI, introduced by
 * a header rather than a comma. Those headers are not ingredients.
 */
object InciAppendixSplitter {
    fun apply(normalizedInci: String): String {
        return HEADER.replace(normalizedInci, ",")
    }

    private val HEADER: Regex = Regex(
        """(?:^|[.,;\s])(?:ALLERGENS|ALERGENY)\s*:?\s*|(?:^|[.,;\s])CONTAINS\s*:\s*"""
    )
}
