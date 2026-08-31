package com.hnexperts.cosmetics.scanning.domain

object ReviewDraftMerger {
    const val MAX_SHOTS: Int = 3

    fun merge(existing: IngredientReviewDraft, incoming: IngredientReviewDraft): IngredientReviewDraft {
        val seen: MutableSet<String> = mutableSetOf()
        val tokens: MutableList<ReviewToken> = mutableListOf()
        var nextKey: Long = existing.nextKey
        for (token in existing.tokens + incoming.tokens) {
            val key: String = InciTokenSet.normalizeSet(token.inciName()).firstOrNull() ?: continue
            if (seen.add(key)) {
                tokens.add(token.copy(key = nextKey))
                nextKey += 1L
            }
        }
        return IngredientReviewDraft(
            rawText = tokens.joinToString(", ") { token -> token.inciName() },
            tokens = tokens,
            nextKey = nextKey,
            usage = existing.usage ?: incoming.usage,
            source = existing.source
        )
    }
}
