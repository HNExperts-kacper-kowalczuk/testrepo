package com.hnexperts.cosmetics.catalog.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.catalog.overlay.PolishProductOverlay
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PolishProductOverlayWriterTest {
    @Test
    fun seedAppliesPolishOverlaySoGs1PolandCodesResolve() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CatalogDatabase.Schema.create(driver)
        val database = CatalogDatabase(driver)
        CatalogSeeder(CatalogWriter(database)).seedIfEmpty()
        val bambino = database.catalogDatabaseQueries
            .selectProductByGtin("5900017071398")
            .executeAsOneOrNull()
        val ziaja = database.catalogDatabaseQueries
            .selectProductByGtin("5901887019367")
            .executeAsOneOrNull()
        assertNotNull(bambino)
        assertNotNull(ziaja)
        assertEquals("Bambino", bambino.brand)
        assertEquals("Ziaja", ziaja.brand)
        assertTrue(bambino.inci_raw.startsWith("Aqua"))
        assertTrue(ziaja.inci_raw.contains("Allantoin"))
    }

    @Test
    fun overlayUpsertIsIdempotent() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CatalogDatabase.Schema.create(driver)
        val database = CatalogDatabase(driver)
        val writer = CatalogWriter(database)
        writer.seedFromFixturesIfNeeded()
        writer.applyProductOverlay(PolishProductOverlay.products)
        writer.applyProductOverlay(PolishProductOverlay.products)
        val rows = database.catalogDatabaseQueries.selectAllProducts().executeAsList()
        val overlayIds = rows.filter { row -> row.id.startsWith("pl-") }
        assertEquals(PolishProductOverlay.products.size, overlayIds.size)
    }
}
