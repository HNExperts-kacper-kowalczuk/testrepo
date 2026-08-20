package com.hnexperts.cosmetics.ads.domain

import com.hnexperts.cosmetics.failure.Outcome

interface BillingPort {
    fun isAvailable(): Boolean
    suspend fun purchaseRemoveAds(): Outcome<Boolean>
}

class NoOpBillingPort : BillingPort {
    override fun isAvailable(): Boolean {
        return false
    }

    override suspend fun purchaseRemoveAds(): Outcome<Boolean> {
        return Outcome.Ok(false)
    }
}
