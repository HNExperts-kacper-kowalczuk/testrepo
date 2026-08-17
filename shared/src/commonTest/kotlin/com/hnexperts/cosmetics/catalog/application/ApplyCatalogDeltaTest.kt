package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ApplyCatalogDeltaTest {
    @Test
    fun appliesNewProductAndReloadsIndex() {
        runBlocking {
            val store = MemoryCatalogStore()
            val gateway = ReloadingGateway(store)
            val extra: FixtureProduct = sampleProduct()
            val nextMeta: CatalogMeta = nextMeta(extra)
            val apply = ApplyCatalogDelta(
                mutations = store,
                deltas = FixedDeltaSource(
                    CatalogDelta(
                        fromCatalogVersion = FixtureCatalog.CATALOG_VERSION,
                        meta = nextMeta,
                        ingredients = emptyList(),
                        products = listOf(extra)
                    )
                ),
                catalog = gateway,
                dispatchers = AppDispatchers(
                    main = Dispatchers.Unconfined,
                    computation = Dispatchers.Unconfined,
                    io = Dispatchers.Unconfined,
                    catalogDatabase = Dispatchers.Unconfined,
                    userDatabase = Dispatchers.Unconfined
                )
            )
            val published = nextMeta.toManifest(productCount = FixtureCatalog.products.size + 1, ingredientCount = FixtureCatalog.ingredients.size)
            val index: CatalogIndex = requireOk(apply.invoke(published))
            assertEquals(nextMeta.catalogVersion, index.meta.catalogVersion)
            assertEquals(nextMeta.checksum, index.meta.checksum)
            assertTrue(store.products.any { item -> item.product.id == extra.product.id })
        }
    }

    @Test
    fun missingDeltaIsCatalogFailure() {
        runBlocking {
            val store = MemoryCatalogStore()
            val apply = ApplyCatalogDelta(
                mutations = store,
                deltas = BundledCatalogDeltaSource(),
                catalog = ReloadingGateway(store),
                dispatchers = AppDispatchers(
                    main = Dispatchers.Unconfined,
                    computation = Dispatchers.Unconfined,
                    io = Dispatchers.Unconfined,
                    catalogDatabase = Dispatchers.Unconfined,
                    userDatabase = Dispatchers.Unconfined
                )
            )
            val published = CatalogIntegrity.fixtureMeta().toManifest(
                productCount = FixtureCatalog.products.size,
                ingredientCount = FixtureCatalog.ingredients.size
            ).copy(catalogVersion = "2099.01")
            val outcome: Outcome<CatalogIndex> = apply.invoke(published)
            assertIs<Outcome.Err>(outcome)
        }
    }

    private fun requireOk(outcome: Outcome<CatalogIndex>): CatalogIndex {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private fun sampleProduct(): FixtureProduct {
        return FixtureProduct(
            product = Product(
                id = "pipeline-sample-balm",
                name = "Pipeline Sample Balm",
                brand = "Fixture Pipeline",
                category = "balm",
                inciRaw = "Aqua, Petrolatum",
                usage = "LEAVE_ON",
                source = "obf",
                verified = false
            ),
            gtins = listOf("5901234999999")
        )
    }

    private fun nextMeta(extra: FixtureProduct): CatalogMeta {
        val checksum: String = CatalogIntegrity.fingerprint(
            catalogVersion = "2026.08-pipeline",
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
            region = CatalogIntegrity.FIXTURE_REGION,
            ingredientIds = FixtureCatalog.ingredients.map { item -> item.ingredient.id },
            productIds = FixtureCatalog.products.map { item -> item.product.id } + extra.product.id
        )
        return CatalogMeta(
            catalogVersion = "2026.08-pipeline",
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
            region = CatalogIntegrity.FIXTURE_REGION,
            checksum = checksum,
            supportedCommentLocales = listOf("en", "pl")
        )
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

    private class FixedDeltaSource(
        private val delta: CatalogDelta
    ) : CatalogDeltaSource {
        override suspend fun deltaFor(fromVersion: String, toVersion: String): CatalogDelta {
            return delta
        }
    }

    private companion object {
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
