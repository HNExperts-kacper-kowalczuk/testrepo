package com.hnexperts.cosmetics.catalog.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.catalog.application.CatalogDelta
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.InciIdentity
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogWriterTest {
    @Test
    fun seedThenDeltaKeepsReadableChecksum() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CatalogDatabase.Schema.create(driver)
        val database = CatalogDatabase(driver)
        val writer = CatalogWriter(database)
        writer.seedFromFixturesIfNeeded()
        val reader = CatalogSnapshotReader(database)
        val seeded = reader.read()
        assertEquals(CatalogIntegrity.fixtureChecksum(), seeded.meta.checksum)

        val extra = FixtureProduct(
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
        val nextVersion = "2026.08-pipeline"
        val checksum = CatalogIntegrity.fingerprint(
            catalogVersion = nextVersion,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
            region = CatalogIntegrity.FIXTURE_REGION,
            ingredientIds = FixtureCatalog.ingredients.map { item -> item.ingredient.id },
            productIds = FixtureCatalog.products.map { item -> item.product.id } + extra.product.id
        )
        writer.applyDelta(
            CatalogDelta(
                fromCatalogVersion = FixtureCatalog.CATALOG_VERSION,
                meta = CatalogMeta(
                    catalogVersion = nextVersion,
                    rulesetVersion = FixtureCatalog.RULESET_VERSION,
                    builtAt = CatalogIntegrity.FIXTURE_BUILT_AT,
                    region = CatalogIntegrity.FIXTURE_REGION,
                    checksum = checksum,
                    supportedCommentLocales = listOf("en", "pl")
                ),
                ingredients = emptyList(),
                products = listOf(extra)
            )
        )
        val updated = reader.read()
        assertEquals(nextVersion, updated.meta.catalogVersion)
        assertEquals(checksum, updated.meta.checksum)
        val names = database.catalogDatabaseQueries.selectAllProducts().executeAsList().map { row -> row.id }
        assertTrue(names.contains("pipeline-sample-balm"))
        val writtenHash: String? = database.catalogDatabaseQueries
            .selectProductById("pipeline-sample-balm")
            .executeAsOne()
            .inci_hash
        assertEquals(InciIdentity.hash("Aqua, Petrolatum"), writtenHash)
        val frequent: List<String> = database.catalogDatabaseQueries
            .selectFrequentCategories(20L)
            .executeAsList()
            .mapNotNull { category -> category }
        assertEquals("balm", frequent.first())
        assertTrue(frequent.contains("moisturizer"))
    }
}
