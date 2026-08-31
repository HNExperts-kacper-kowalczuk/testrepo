package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod

enum class FuzzyDecision {
    NOT_APPLICABLE,
    PENDING,
    ACCEPTED,
    AUTO_ACCEPTED,
    REJECTED
}

fun FuzzyDecision.appliesSuggestion(): Boolean {
    return this == FuzzyDecision.ACCEPTED || this == FuzzyDecision.AUTO_ACCEPTED
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
        if (matchMethod == MatchMethod.FUZZY && !fuzzyDecision.appliesSuggestion()) {
            return rawText
        }
        if (suggestedName.isNotBlank() && matchedIngredientId != null && fuzzyDecision != FuzzyDecision.REJECTED) {
            return suggestedName
        }
        return rawText
    }

    fun canPickFromCatalog(): Boolean {
        return fuzzyDecision == FuzzyDecision.PENDING || matchMethod == MatchMethod.UNMATCHED
    }

    fun withCatalogPick(id: String, inciName: String): ReviewToken {
        return copy(
            suggestedName = inciName,
            matchedIngredientId = id,
            matchMethod = MatchMethod.EXACT,
            fuzzyDecision = FuzzyDecision.NOT_APPLICABLE
        )
    }

    fun needsReview(): Boolean {
        if (fuzzyDecision == FuzzyDecision.PENDING || fuzzyDecision == FuzzyDecision.AUTO_ACCEPTED) {
            return true
        }
        return matchMethod == MatchMethod.UNMATCHED && rawText.isNotBlank()
    }
}

data class IngredientReviewDraft(
    val rawText: String,
    val tokens: List<ReviewToken>,
    val nextKey: Long,
    val usage: ProductUsage? = null,
    val source: String = "ocr"
) {
    fun toInciRaw(): String {
        return tokens.map { token -> token.inciName() }.filter { name -> name.isNotBlank() }.joinToString(", ")
    }

    fun hasPendingFuzzy(): Boolean {
        return tokens.any { token -> token.fuzzyDecision == FuzzyDecision.PENDING }
    }

    fun hasAutoFilledFuzzy(): Boolean {
        return tokens.any { token -> token.fuzzyDecision == FuzzyDecision.AUTO_ACCEPTED }
    }

    fun needsReview(): Boolean {
        return tokens.any { token -> token.needsReview() }
    }
}
