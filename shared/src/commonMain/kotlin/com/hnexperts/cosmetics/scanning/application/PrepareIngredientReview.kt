package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.ingredients.domain.FuzzyHit
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef
import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import com.hnexperts.cosmetics.ingredients.domain.MatchedToken
import com.hnexperts.cosmetics.scanning.domain.FuzzyAutoAccept
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
            val matches: List<MatchedToken> = index.matcher.matchDetailedListConcurrently(trimmed)
            toDraft(trimmed, rawTokens, matches)
        }
    }

    private fun toDraft(
        rawText: String,
        rawTokens: List<String>,
        matches: List<MatchedToken>
    ): IngredientReviewDraft {
        val tokens: List<ReviewToken> = rawTokens.mapIndexed { index, raw ->
            val matched: MatchedToken = matches.getOrElse(index) {
                MatchedToken(
                    reference = IngredientRef(id = null, displayName = raw, matchedBy = MatchMethod.UNMATCHED)
                )
            }
            val reference: IngredientRef = matched.reference
            ReviewToken(
                key = index.toLong() + 1L,
                rawText = raw,
                suggestedName = reference.displayName,
                matchedIngredientId = reference.id,
                matchMethod = reference.matchedBy,
                fuzzyDecision = fuzzyDecisionOf(matched, raw)
            )
        }
        return IngredientReviewDraft(
            rawText = rawText,
            tokens = tokens,
            nextKey = tokens.size.toLong() + 1L
        )
    }

    private fun fuzzyDecisionOf(matched: MatchedToken, raw: String): FuzzyDecision {
        if (matched.reference.matchedBy != MatchMethod.FUZZY) {
            return FuzzyDecision.NOT_APPLICABLE
        }
        val hit: FuzzyHit = matched.fuzzy ?: return FuzzyDecision.PENDING
        val normalizedLength: Int = InciNormalizer.stripNanoSuffix(InciNormalizer.normalize(raw)).length
        return FuzzyAutoAccept.decision(hit, normalizedLength)
    }
}
