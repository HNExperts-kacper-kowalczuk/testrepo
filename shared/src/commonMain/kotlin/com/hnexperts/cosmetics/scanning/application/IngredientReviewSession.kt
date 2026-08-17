package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class IngredientReviewSession {
    private val mutex: Mutex = Mutex()
    private var draft: IngredientReviewDraft? = null

    suspend fun publish(next: IngredientReviewDraft) {
        mutex.withLock { draft = next }
    }

    suspend fun current(): IngredientReviewDraft? {
        return mutex.withLock { draft }
    }
}
