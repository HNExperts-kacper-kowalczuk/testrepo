package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.scanning.domain.FuzzyDecision
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import com.hnexperts.cosmetics.scanning.domain.ReviewToken

class PrepareIngredientReview(
    private val catalog: CatalogGateway
) {
    suspend fun invoke(rawText: String): Outcome<IngredientReviewDraft> {
        val trimmed: String = rawText.trim()
        if (trimmed.isEmpty()) {
            return Outcome.Err(
                AppFailure.Ocr(
                    operation = "ocr.empty",
                    detail = "No text was recognized. Flatten the label, use torch, and capture again."
                )
            )
        }
        val indexOutcome = catalog.awaitIndex()
        val index = when (indexOutcome) {
            is Outcome.Err -> return indexOutcome
            is Outcome.Ok -> indexOutcome.value
        }
        return FailureCatcher.ocr("ocr.prepareReview") {
            val rawTokens: List<String> = index.matcher.tokenize(trimmed)
            val references: List<IngredientRef> = index.matcher.matchListConcurrently(trimmed)
            toDraft(trimmed, rawTokens, references)
        }
    }

    private fun toDraft(
        rawText: String,
        rawTokens: List<String>,
        references: List<IngredientRef>
    ): IngredientReviewDraft {
        val tokens: List<ReviewToken> = rawTokens.mapIndexed { index, raw ->
            val reference: IngredientRef = references.getOrElse(index) {
                IngredientRef(id = null, displayName = raw, matchedBy = MatchMethod.UNMATCHED)
            }
            ReviewToken(
                key = index.toLong() + 1L,
                rawText = raw,
                suggestedName = reference.displayName,
                matchedIngredientId = reference.id,
                matchMethod = reference.matchedBy,
                fuzzyDecision = fuzzyDecisionOf(reference.matchedBy)
            )
        }
        return IngredientReviewDraft(
            rawText = rawText,
            tokens = tokens,
            nextKey = tokens.size.toLong() + 1L
        )
    }

    private fun fuzzyDecisionOf(method: MatchMethod): FuzzyDecision {
        return if (method == MatchMethod.FUZZY) {
            FuzzyDecision.PENDING
        } else {
            FuzzyDecision.NOT_APPLICABLE
        }
    }
}
