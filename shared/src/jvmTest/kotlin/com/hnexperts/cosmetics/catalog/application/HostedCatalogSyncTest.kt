package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.catalog.pipeline.CatalogSourceCodec
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.crypto.Sha256
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class HostedCatalogSyncTest {
    @Test
    fun matchingChecksumAppliesDeltaProduct() = runBlocking {
        val extra: FixtureProduct = sampleProduct()
        val delta: CatalogDelta = hostedDelta(extra)
        val gzip: ByteArray = gzip(CatalogSourceCodec.encodeDelta(delta).encodeToByteArray())
        val published: CatalogManifest = publishedManifest(delta.meta, Sha256.hex(gzip))
        val store = MemoryCatalogStore()
        val apply = applyDelta(store, gzipHttp(gzip), BASE)
        val index: CatalogIndex = requireOk(apply.invoke(published))
        assertEquals(delta.meta.catalogVersion, index.meta.catalogVersion)
        assertTrue(store.products.any { item -> item.product.id == extra.product.id })
    }

    @Test
    fun checksumMismatchDoesNotReplaceCatalog() = runBlocking {
        val extra: FixtureProduct = sampleProduct()
        val delta: CatalogDelta = hostedDelta(extra)
        val gzip: ByteArray = gzip(CatalogSourceCodec.encodeDelta(delta).encodeToByteArray())
        val published: CatalogManifest = publishedManifest(delta.meta, "0".repeat(64))
        val store = MemoryCatalogStore()
        val before: Int = store.products.size
        val apply = applyDelta(store, gzipHttp(gzip), BASE)
        val outcome: Outcome<CatalogIndex> = apply.invoke(published)
        assertIs<Outcome.Err>(outcome)
        assertIs<AppFailure.CorruptCatalog>(outcome.failure)
        assertEquals(before, store.products.size)
        assertFalse(store.products.any { item -> item.product.id == extra.product.id })
        assertEquals(FixtureCatalog.CATALOG_VERSION, store.meta.catalogVersion)
    }

    private fun applyDelta(
        store: MemoryCatalogStore,
        http: SimpleHttpClient,
        baseUrl: String
    ): ApplyCatalogDelta {
        return ApplyCatalogDelta(
            mutations = store,
            deltas = HttpCatalogDeltaSource(http, baseUrl),
            catalog = ReloadingGateway(store),
            dispatchers = AppDispatchers(
                main = Dispatchers.Unconfined,
                computation = Dispatchers.Unconfined,
                io = Dispatchers.Unconfined,
                catalogDatabase = Dispatchers.Unconfined,
                userDatabase = Dispatchers.Unconfined
            )
        )
    }

    private fun gzipHttp(gzip: ByteArray): SimpleHttpClient {
        val url: String = CatalogSyncPaths.deltaUrl(
            BASE,
            FixtureCatalog.CATALOG_VERSION,
            "2026.09-hosted"
        )
        return MapBytesHttp(mapOf(url to gzip))
    }

    private fun requireOk(outcome: Outcome<CatalogIndex>): CatalogIndex {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private class MapBytesHttp(
        private val bodies: Map<String, ByteArray>
    ) : SimpleHttpClient {
        override suspend fun getText(url: String): Outcome<String> {
            return Outcome.Err(AppFailure.Network(operation = "http.get", detail = "unused"))
        }

        override suspend fun getBytes(url: String): Outcome<ByteArray> {
            val body: ByteArray = bodies[url]
                ?: return Outcome.Err(AppFailure.Network(operation = "http.get.bytes", detail = "missing $url"))
            return Outcome.Ok(body)
        }
    }

    private class MemoryCatalogStore : CatalogMutationStore {
        var ingredients: MutableList<FixtureIngredient> = FixtureCatalog.ingredients.toMutableList()
        var products: MutableList<FixtureProduct> = FixtureCatalog.products.toMutableList()
        var meta: CatalogMeta = CatalogIntegrity.fixtureMeta()

        override fun hasCurrentCatalog(version: String, checksum: String): Boolean {
            return meta.catalogVersion == version && meta.checksum == checksum
        }

        override fun isEmpty(): Boolean = false

        override fun replaceAll(
            ingredients: List<FixtureIngredient>,
            products: List<FixtureProduct>,
            meta: CatalogMeta
        ) {
            this.ingredients = ingredients.toMutableList()
            this.products = products.toMutableList()
            this.meta = meta
        }

        override fun applyDelta(delta: CatalogDelta) {
            for (ingredient in delta.ingredients) {
                ingredients.removeAll { item -> item.ingredient.id == ingredient.ingredient.id }
                ingredients.add(ingredient)
            }
            for (product in delta.products) {
                products.removeAll { item -> item.product.id == product.product.id }
                products.add(product)
            }
            meta = delta.meta
        }

        override fun upsertMeta(meta: CatalogMeta) {
            this.meta = meta
        }

        override fun clearAll() {
            ingredients.clear()
            products.clear()
        }
    }

    private class ReloadingGateway(
        private val store: MemoryCatalogStore
    ) : CatalogGateway {
        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            return Outcome.Ok(indexFrom(store))
        }

        override suspend fun reload(): Outcome<CatalogIndex> {
            return awaitIndex()
        }
    }

    private companion object {
        const val BASE: String = "https://catalog.example.test"

        fun gzip(input: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            GZIPOutputStream(output).use { stream -> stream.write(input) }
            return output.toByteArray()
        }

        fun sampleProduct(): FixtureProduct {
            return FixtureProduct(
                product = Product(
                    id = "hosted-sample-balm",
                    name = "Hosted Sample Balm",
                    brand = "Fixture Pipeline",
                    category = "balm",
                    inciRaw = "Aqua, Petrolatum",
                    usage = "LEAVE_ON",
                    source = "obf",
                    verified = false
                ),
                gtins = listOf("5901234999998")
            )
        }

        fun hostedDelta(extra: FixtureProduct): CatalogDelta {
            val checksum: String = CatalogIntegrity.fingerprint(
                catalogVersion = "2026.09-hosted",
                rulesetVersion = FixtureCatalog.RULESET_VERSION,
                builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
                region = CatalogIntegrity.FIXTURE_REGION,
                ingredientIds = FixtureCatalog.ingredients.map { item -> item.ingredient.id },
                productIds = FixtureCatalog.products.map { item -> item.product.id } + extra.product.id
            )
            val meta = CatalogMeta(
                catalogVersion = "2026.09-hosted",
                rulesetVersion = FixtureCatalog.RULESET_VERSION,
                builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
                region = CatalogIntegrity.FIXTURE_REGION,
                checksum = checksum,
                supportedCommentLocales = listOf("en", "pl")
            )
            return CatalogDelta(
                fromCatalogVersion = FixtureCatalog.CATALOG_VERSION,
                meta = meta,
                ingredients = emptyList(),
                products = listOf(extra)
            )
        }

        fun publishedManifest(meta: CatalogMeta, payloadSha256: String): CatalogManifest {
            return CatalogManifest(
                catalogVersion = meta.catalogVersion,
                rulesetVersion = meta.rulesetVersion,
                builtAt = meta.builtAt,
                region = meta.region,
                checksum = payloadSha256,
                productCount = FixtureCatalog.products.size + 1,
                ingredientCount = FixtureCatalog.ingredients.size
            )
        }

        fun indexFrom(store: MemoryCatalogStore): CatalogIndex {
            return CatalogIndex.assemble(
                CatalogSnapshot(
                    meta = store.meta,
                    ingredients = store.ingredients.map { item -> item.ingredient },
                    aliases = FixtureCatalog.aliasMap(),
                    commaExceptions = FixtureCatalog.commaExceptions(),
                    hazards = store.ingredients.associate { item -> item.ingredient.id to item.hazard },
                    comments = store.ingredients.associate { item -> item.ingredient.id to item.comments }
                )
            )
        }
    }
}
