package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class CheckCatalogUpdatesTest {
    @Test
    fun offlineDoesNotTouchRemote() {
        runBlocking {
            val check = CheckCatalogUpdates(
                catalog = MissingCatalog,
                remote = ThrowingRemote,
                network = OfflineNetwork
            )
            val freshness: CatalogFreshness = requireOk(check.invoke())
            assertIs<CatalogFreshness.Offline>(freshness)
        }
    }

    @Test
    fun differentRemoteVersionIsUpdateAvailable() {
        runBlocking {
            val index: CatalogIndex = CatalogIndex.assemble(
                CatalogSnapshot(
                    meta = CatalogIntegrity.fixtureMeta(),
                    ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
                    aliases = FixtureCatalog.aliasMap(),
                    commaExceptions = FixtureCatalog.commaExceptions(),
                    hazards = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.hazard },
                    comments = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.comments }
                )
            )
            val published = CatalogIntegrity.fixtureMeta().toManifest(
                productCount = FixtureCatalog.products.size,
                ingredientCount = FixtureCatalog.ingredients.size
            ).copy(catalogVersion = "2099.01", checksum = "abc")
            val check = CheckCatalogUpdates(
                catalog = FixedCatalog(index),
                remote = object : CatalogRemote {
                    override suspend fun publishedManifest(): CatalogManifest = published
                },
                network = OnlineNetwork
            )
            val freshness: CatalogFreshness = requireOk(check.invoke())
            assertIs<CatalogFreshness.UpdateAvailable>(freshness)
            assertEquals("2099.01", freshness.published.catalogVersion)
        }
    }

    @Test
    fun bundledManifestIsUpToDate() {
        runBlocking {
            val index: CatalogIndex = CatalogIndex.assemble(
                CatalogSnapshot(
                    meta = CatalogIntegrity.fixtureMeta(),
                    ingredients = FixtureCatalog.ingredients.map { item -> item.ingredient },
                    aliases = FixtureCatalog.aliasMap(),
                    commaExceptions = FixtureCatalog.commaExceptions(),
                    hazards = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.hazard },
                    comments = FixtureCatalog.ingredients.associate { item -> item.ingredient.id to item.comments }
                )
            )
            val check = CheckCatalogUpdates(
                catalog = FixedCatalog(index),
                remote = BundledCatalogRemote(),
                network = OnlineNetwork
            )
            val freshness: CatalogFreshness = requireOk(check.invoke())
            assertIs<CatalogFreshness.UpToDate>(freshness)
            assertEquals(FixtureCatalog.CATALOG_VERSION, freshness.manifest.catalogVersion)
        }
    }

    private fun requireOk(outcome: Outcome<CatalogFreshness>): CatalogFreshness {
        assertTrue(outcome is Outcome.Ok, outcome.toString())
        return outcome.value
    }

    private object OfflineNetwork : NetworkMonitor {
        override fun isOnline(): Boolean = false
    }

    private object OnlineNetwork : NetworkMonitor {
        override fun isOnline(): Boolean = true
    }

    private object MissingCatalog : CatalogGateway {
        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            error("catalog should not load while offline")
        }
    }

    private object ThrowingRemote : CatalogRemote {
        override suspend fun publishedManifest(): CatalogManifest {
            error("remote should not be called while offline")
        }
    }

    private class FixedCatalog(
        private val index: CatalogIndex
    ) : CatalogGateway {
        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            return Outcome.Ok(index)
        }
    }
}
