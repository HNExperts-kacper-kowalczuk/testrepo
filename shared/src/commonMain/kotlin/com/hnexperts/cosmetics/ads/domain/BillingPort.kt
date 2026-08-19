package com.hnexperts.cosmetics.ads.domain

import com.hnexperts.cosmetics.failure.Outcome

interface BillingPort {
    suspend fun purchaseRemoveAds(): Outcome<Boolean>
}

class NoOpBillingPort : BillingPort {
    override suspend fun purchaseRemoveAds(): Outcome<Boolean> {
        return Outcome.Ok(false)
    }
}
