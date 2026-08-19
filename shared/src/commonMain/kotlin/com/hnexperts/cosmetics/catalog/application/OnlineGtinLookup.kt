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
        val beauty: Outcome<String> = http.getText("$OBF_PRODUCT$gtin.json")
        val fromBeauty: OnlineGtinHit? = parseIfOk(gtin, beauty)
        if (fromBeauty is OnlineGtinHit.WithIngredients) {
            return Outcome.Ok(fromBeauty)
        }
        val food: Outcome<String> = http.getText("$OFF_PRODUCT$gtin.json")
        val fromFood: OnlineGtinHit? = parseIfOk(gtin, food)
        val hit: OnlineGtinHit = when {
            fromFood is OnlineGtinHit.WithIngredients -> fromFood
            fromBeauty is OnlineGtinHit.MissingIngredients -> fromBeauty
            fromFood is OnlineGtinHit.MissingIngredients -> fromFood
            fromBeauty != null -> fromBeauty
            fromFood != null -> fromFood
            beauty is Outcome.Err -> return beauty
            food is Outcome.Err -> return food
            else -> OnlineGtinHit.NotFound(gtin)
        }
        return Outcome.Ok(hit)
    }

    private fun parseIfOk(gtin: String, body: Outcome<String>): OnlineGtinHit? {
        return when (body) {
            is Outcome.Ok -> ObfProductParser.parse(gtin, body.value)
            is Outcome.Err -> null
        }
    }

    private companion object {
        const val OBF_PRODUCT: String = "https://world.openbeautyfacts.org/api/v2/product/"
        const val OFF_PRODUCT: String = "https://world.openfoodfacts.org/api/v2/product/"
    }
}
