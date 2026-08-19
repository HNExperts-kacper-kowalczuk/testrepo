package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class ResolveGtinTest {
    private val catalogProduct: Product = Product(
        id = "gentle-cleanser",
        name = "Gentle Cream Cleanser",
        brand = "Fixture Care",
        category = "cleanser",
        inciRaw = "Aqua, Glycerin",
        usage = "RINSE_OFF",
        source = "curated",
        verified = true
    )

    @Test
    fun catalogHitDoesNotCallTheNetwork() = runBlocking {
        val http = RecordingHttp()
        val resolve = resolver(mapOf("5901234123457" to catalogProduct), http, online = true)
        val result = requireOk(resolve.invoke("5901234123457"))
        val ready = assertIs<GtinResolution.ReadyToEvaluate>(result)
        assertEquals("barcode", ready.source)
        assertEquals("Aqua, Glycerin", ready.inciRaw)
        assertEquals(emptyList(), http.urls)
    }

    @Test
    fun catalogMissFetchesIngredientsOnlineAndReturnsThemReadyToScore() = runBlocking {
        val http = RecordingHttp(
            mapOf(
                "https://world.openbeautyfacts.org/api/v2/product/4000000000001.json" to
                    """{"status":1,"product":{"product_name":"Online Cream","brands":"TestCo","ingredients_text":"Aqua, Glycerin, Niacinamide, Panthenol"}}"""
            )
        )
        val resolve = resolver(emptyMap(), http, online = true)
        val result = requireOk(resolve.invoke("4000000000001"))
        val ready = assertIs<GtinResolution.ReadyToEvaluate>(result)
        assertEquals("online", ready.source)
        assertEquals("Online Cream", ready.productName)
        assertEquals("TestCo", ready.brand)
        assertTrue(ready.inciRaw.startsWith("Aqua"))
    }

    @Test
    fun offlineCatalogMissSkipsTheNetwork() = runBlocking {
        val http = RecordingHttp()
        val resolve = resolver(emptyMap(), http, online = false)
        val result = requireOk(resolve.invoke("4000000000001"))
        val unknown = assertIs<GtinResolution.Unknown>(result)
        assertFalse(unknown.onlineNoIngredients)
        assertEquals(emptyList(), http.urls)
    }

    @Test
    fun onlineListingWithoutInciStaysUnknown() = runBlocking {
        val http = RecordingHttp(
            mapOf(
                "https://world.openbeautyfacts.org/api/v2/product/1.json" to
                    """{"status":1,"product":{"product_name":"No list"}}""",
                "https://world.openfoodfacts.org/api/v2/product/1.json" to
                    """{"status":0}"""
            )
        )
        val resolve = resolver(emptyMap(), http, online = true)
        val result = requireOk(resolve.invoke("1"))
        val unknown = assertIs<GtinResolution.Unknown>(result)
        assertTrue(unknown.onlineNoIngredients)
    }

    @Test
    fun onlineHttpErrorFallsThroughToUnknownWithoutBlocking() = runBlocking {
        val resolve = ResolveGtin(
            offline = ResolveBarcode(MemoryProducts(emptyMap())),
            online = OnlineGtinLookup(FailingHttp(), Online)
        )
        val result = requireOk(resolve.invoke("4000000000001"))
        val unknown = assertIs<GtinResolution.Unknown>(result)
        assertFalse(unknown.onlineNoIngredients)
    }

    private fun resolver(
        catalog: Map<String, Product>,
        http: RecordingHttp,
        online: Boolean
    ): ResolveGtin {
        return ResolveGtin(
            offline = ResolveBarcode(MemoryProducts(catalog)),
            online = OnlineGtinLookup(http, if (online) Online else Offline)
        )
    }

    private fun requireOk(outcome: Outcome<GtinResolution>): GtinResolution {
        assertTrue(outcome is Outcome.Ok)
        return outcome.value
    }

    private object Online : NetworkMonitor {
        override fun isOnline(): Boolean = true
    }

    private object Offline : NetworkMonitor {
        override fun isOnline(): Boolean = false
    }

    private class MemoryProducts(
        private val byGtin: Map<String, Product>
    ) : ProductRepository {
        override suspend fun findByGtin(rawGtin: String): Outcome<Product?> {
            return Outcome.Ok(byGtin[rawGtin])
        }

        override suspend fun search(query: String): Outcome<List<Product>> {
            return Outcome.Ok(emptyList())
        }
    }

    private class FailingHttp : SimpleHttpClient {
        override suspend fun getText(url: String): Outcome<String> {
            return Outcome.Err(AppFailure.Network(operation = "http.get", detail = "timeout"))
        }
    }

    private class RecordingHttp(
        private val bodies: Map<String, String> = emptyMap()
    ) : SimpleHttpClient {
        val urls: MutableList<String> = mutableListOf()

        override suspend fun getText(url: String): Outcome<String> {
            urls.add(url)
            return Outcome.Ok(bodies[url] ?: """{"status":0}""")
        }
    }
}
