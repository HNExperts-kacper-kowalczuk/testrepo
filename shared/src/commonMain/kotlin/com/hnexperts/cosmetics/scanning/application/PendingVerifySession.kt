package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.scanning.domain.IngredientReviewDraft
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class VerifyRequest(
    val gtin: String?,
    val catalogInci: String,
    val productName: String?,
    val brand: String?,
    val usage: ProductUsage,
    val source: String
)

class PendingVerifySession {
    private val mutex: Mutex = Mutex()
    private var request: VerifyRequest? = null
    private var mergeDraft: IngredientReviewDraft? = null
    private var unknownGtin: String? = null
    private var photoCount: Int = 0

    suspend fun rememberUnknownGtin(gtin: String) {
        mutex.withLock { unknownGtin = gtin }
    }

    suspend fun unknownGtin(): String? {
        return mutex.withLock { unknownGtin }
    }

    suspend fun publishVerify(next: VerifyRequest) {
        mutex.withLock {
            request = next
            mergeDraft = null
            photoCount = 0
        }
    }

    suspend fun currentVerify(): VerifyRequest? {
        return mutex.withLock { request }
    }

    suspend fun clearVerify() {
        mutex.withLock {
            request = null
            mergeDraft = null
            unknownGtin = null
            photoCount = 0
        }
    }

    suspend fun stashDraft(draft: IngredientReviewDraft) {
        mutex.withLock { mergeDraft = draft }
    }

    suspend fun takeStashedDraft(): IngredientReviewDraft? {
        return mutex.withLock {
            val current: IngredientReviewDraft? = mergeDraft
            mergeDraft = null
            current
        }
    }

    suspend fun notePhoto(): Int {
        return mutex.withLock {
            photoCount += 1
            photoCount
        }
    }

    suspend fun photosTaken(): Int {
        return mutex.withLock { photoCount }
    }
}
