package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft

sealed class TypedIngredientReview {
    data class Confirm(val draft: IngredientReviewDraft) : TypedIngredientReview()
    data class Ready(val inciRaw: String, val usage: ProductUsage) : TypedIngredientReview()
}

class OpenTypedIngredientReview(
    private val prepareReview: PrepareIngredientReview,
    private val reviewSession: IngredientReviewSession
) {
    suspend fun invoke(rawText: String, usage: ProductUsage): Outcome<TypedIngredientReview> {
        val prepared: Outcome<IngredientReviewDraft> = prepareReview.invoke(rawText)
        val draft: IngredientReviewDraft = when (prepared) {
            is Outcome.Err -> return prepared
            is Outcome.Ok -> prepared.value.copy(usage = usage, source = SOURCE_MANUAL)
        }
        if (draft.needsReview()) {
            reviewSession.publish(draft)
            return Outcome.Ok(TypedIngredientReview.Confirm(draft))
        }
        return Outcome.Ok(TypedIngredientReview.Ready(inciRaw = draft.toInciRaw(), usage = usage))
    }

    private companion object {
        const val SOURCE_MANUAL: String = "manual"
    }
}
