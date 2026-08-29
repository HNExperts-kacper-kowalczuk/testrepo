package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient

class OnlineGtinLookup(
    private val http: SimpleHttpClient,
    private val network: NetworkMonitor
) {
    suspend fun invoke(gtin: String): Outcome<OnlineGtinHit> {
        if (!network.isOnline()) {
            return Outcome.Ok(OnlineGtinHit.NotFound(gtin))
        }
        return firstHit(gtin)
    }

    private suspend fun firstHit(gtin: String): Outcome<OnlineGtinHit> {
        var fallback: OnlineGtinHit = OnlineGtinHit.NotFound(gtin)
        var firstErr: Outcome.Err? = null
        var anyBody: Boolean = false
        for (url in GtinLookupEndpoints.productJsonUrls(gtin)) {
            when (val body: Outcome<String> = http.getText(url)) {
                is Outcome.Ok -> {
                    anyBody = true
                    val hit: OnlineGtinHit = ObfProductParser.parse(gtin, body.value)
                    if (hit is OnlineGtinHit.WithIngredients) {
                        return Outcome.Ok(hit)
                    }
                    fallback = strongerMiss(fallback, hit)
                }
                is Outcome.Err -> if (firstErr == null) firstErr = body
            }
        }
        val err: Outcome.Err? = firstErr
        return if (!anyBody && err != null) err else Outcome.Ok(fallback)
    }

    private fun strongerMiss(current: OnlineGtinHit, next: OnlineGtinHit): OnlineGtinHit {
        if (current is OnlineGtinHit.MissingIngredients) {
            return current
        }
        if (next is OnlineGtinHit.MissingIngredients) {
            return next
        }
        return current
    }
}
