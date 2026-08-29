package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserSchemaGuardTest {
    @Test
    fun ensureOnCurrentSchemaIsIdempotent() {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        UserDatabase.Schema.create(driver)
        UserSchemaGuard.ensure(driver)
        UserSchemaGuard.ensure(driver)
        assertTrue(SqliteInspect.columnExists(driver, "user_profile", "theme_preference"))
        assertTrue(SqliteInspect.columnExists(driver, "scan_history", "category"))
        assertTrue(SqliteInspect.columnExists(driver, "user_shelf", "shelf_key"))
        assertTrue(SqliteInspect.tableExists(driver, "cached_online_product"))
    }

    @Test
    fun ensureAddsMissingProfileAndHistoryColumns() {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        createLegacyUserTables(driver)
        assertFalse(SqliteInspect.columnExists(driver, "user_profile", "eu_allergens"))
        assertFalse(SqliteInspect.columnExists(driver, "scan_history", "name"))
        UserSchemaGuard.ensure(driver)
        assertTrue(SqliteInspect.columnExists(driver, "user_profile", "eu_allergens"))
        assertTrue(SqliteInspect.columnExists(driver, "user_profile", "theme_preference"))
        assertTrue(SqliteInspect.columnExists(driver, "scan_history", "name"))
        assertTrue(SqliteInspect.columnExists(driver, "user_shelf", "shelf_key"))
        val keys: Set<String> = SqliteInspect.columns(driver, "user_shelf")
        assertTrue(keys.contains("inci_hash"))
    }

    @Test
    fun packedCatalogTablesAreDetectedWithoutRecreate() {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CatalogDatabase.Schema.create(driver)
        assertTrue(SqliteInspect.tableExists(driver, "catalog_meta"))
        val failed: Boolean = secondCreateFails(driver)
        assertTrue(failed)
    }

    private fun secondCreateFails(driver: SqlDriver): Boolean {
        return try {
            CatalogDatabase.Schema.create(driver)
            false
        } catch (_: Exception) {
            true
        }
    }

    private fun createLegacyUserTables(driver: SqlDriver) {
        execute(
            driver,
            """
            CREATE TABLE user_profile (
                id INTEGER NOT NULL PRIMARY KEY,
                pregnancy_caution INTEGER NOT NULL DEFAULT 0,
                fragrance_free INTEGER NOT NULL DEFAULT 0,
                locale_preference TEXT NOT NULL DEFAULT 'system',
                pinned_locale TEXT
            )
            """
        )
        execute(
            driver,
            """
            CREATE TABLE scan_history (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                scanned_at TEXT NOT NULL,
                gtin TEXT,
                product_id TEXT,
                inci_raw TEXT NOT NULL,
                rating TEXT NOT NULL,
                source TEXT NOT NULL
            )
            """
        )
        execute(
            driver,
            """
            CREATE TABLE user_shelf (
                product_id TEXT,
                gtin TEXT,
                inci_raw TEXT NOT NULL,
                saved_at TEXT NOT NULL
            )
            """
        )
    }

    private fun execute(driver: SqlDriver, sql: String) {
        driver.execute(identifier = null, sql = sql, parameters = 0)
    }
}
