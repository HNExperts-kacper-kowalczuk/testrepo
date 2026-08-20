package com.hnexperts.cosmetics.ads.domain

import com.hnexperts.cosmetics.failure.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.runBlocking

class NoOpBillingPortTest {
    @Test
    fun isUnavailableAndDoesNotPurchase() = runBlocking {
        val billing: BillingPort = NoOpBillingPort()
        assertFalse(billing.isAvailable())
        assertEquals(Outcome.Ok(false), billing.purchaseRemoveAds())
    }
}
