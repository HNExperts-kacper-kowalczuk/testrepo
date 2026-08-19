package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class OnlineGtinLookupTest {
    @Test
    fun offlineDeviceDoesNotHitTheNetwork() = runBlocking {
        val http = RecordingHttp()
        val lookup = OnlineGtinLookup(http, Offline)
        val result: Outcome<OnlineGtinHit> = lookup.invoke("5901234123457")
        val err = assertIs<Outcome.Err>(result)
        assertIs<AppFailure.Network>(err.failure)
        assertEquals(emptyList(), http.urls)
    }

    @Test
    fun usesOpenBeautyFactsWhenTheProductHasIngredients() = runBlocking {
        val http = RecordingHttp(
            mapOf(
                "https://world.openbeautyfacts.org/api/v2/product/5901234123457.json" to
                    """{"status":1,"product":{"product_name":"Cream","ingredients_text":"Aqua, Glycerin, Panthenol, Niacinamide"}}"""
            )
        )
        val lookup = OnlineGtinLookup(http, Online)
        val result: Outcome<OnlineGtinHit> = lookup.invoke("5901234123457")
        val hit = assertIs<OnlineGtinHit.WithIngredients>((result as Outcome.Ok).value)
        assertEquals("Cream", hit.name)
        assertEquals(1, http.urls.size)
    }

    @Test
    fun fallsBackToOpenFoodFactsWhenBeautyFactsHasNoIngredients() = runBlocking {
        val http = RecordingHttp(
            mapOf(
                "https://world.openbeautyfacts.org/api/v2/product/1.json" to
                    """{"status":1,"product":{"product_name":"Empty"}}""",
                "https://world.openfoodfacts.org/api/v2/product/1.json" to
                    """{"status":1,"product":{"product_name":"Food-adjacent","ingredients_text":"Aqua, Glycerin, Sodium Chloride, Parfum"}}"""
            )
        )
        val lookup = OnlineGtinLookup(http, Online)
        val result: Outcome<OnlineGtinHit> = lookup.invoke("1")
        val hit = assertIs<OnlineGtinHit.WithIngredients>((result as Outcome.Ok).value)
        assertEquals("Food-adjacent", hit.name)
    }

    @Test
    fun webSearchUrlContainsTheGtin() {
        val lookup = OnlineGtinLookup(RecordingHttp(), Online)
        assertTrue(lookup.webSearchUrl("5901234123457").contains("5901234123457"))
    }

    private object Online : NetworkMonitor {
        override fun isOnline(): Boolean = true
    }

    private object Offline : NetworkMonitor {
        override fun isOnline(): Boolean = false
    }

    private class RecordingHttp(
        private val bodies: Map<String, String> = emptyMap()
    ) : SimpleHttpClient {
        val urls: MutableList<String> = mutableListOf()

        override suspend fun getText(url: String): Outcome<String> {
            urls.add(url)
            val body: String = bodies[url] ?: """{"status":0}"""
            return Outcome.Ok(body)
        }
    }
}
