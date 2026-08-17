package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.ingredients.domain.MatchMethod

enum class FuzzyDecision {
    NOT_APPLICABLE,
    PENDING,
    ACCEPTED,
    REJECTED
}

data class ReviewToken(
    val key: Long,
    val rawText: String,
    val suggestedName: String,
    val matchedIngredientId: String?,
    val matchMethod: MatchMethod,
    val fuzzyDecision: FuzzyDecision
) {
    fun inciName(): String {
        if (matchMethod == MatchMethod.FUZZY && fuzzyDecision == FuzzyDecision.REJECTED) {
            return rawText
        }
        if (matchMethod == MatchMethod.FUZZY && fuzzyDecision != FuzzyDecision.ACCEPTED) {
            return rawText
        }
        if (suggestedName.isNotBlank() && matchedIngredientId != null && fuzzyDecision != FuzzyDecision.REJECTED) {
            return suggestedName
        }
        return rawText
    }
}

data class IngredientReviewDraft(
    val rawText: String,
    val tokens: List<ReviewToken>,
    val nextKey: Long
) {
    fun toInciRaw(): String {
        return tokens.map { token -> token.inciName() }.filter { name -> name.isNotBlank() }.joinToString(", ")
    }

    fun hasPendingFuzzy(): Boolean {
        return tokens.any { token -> token.fuzzyDecision == FuzzyDecision.PENDING }
    }
}
