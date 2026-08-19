package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CancellationException

object UserSchemaGuard {
    fun ensure(driver: SqlDriver) {
        createCachedProductTable(driver)
        addProfileColumns(driver)
        migrateHistoryTable(driver)
        migrateShelfTable(driver)
    }

    private fun createCachedProductTable(driver: SqlDriver) {
        executeIgnoringExisting(driver, CACHED_PRODUCT_TABLE)
    }

    private fun addProfileColumns(driver: SqlDriver) {
        PROFILE_COLUMNS.forEach { column ->
            addColumn(driver, table = "user_profile", columnSql = column)
        }
    }

    private fun migrateHistoryTable(driver: SqlDriver) {
        HISTORY_COLUMNS.forEach { column ->
            addColumn(driver, table = "scan_history", columnSql = column)
        }
    }

    private fun migrateShelfTable(driver: SqlDriver) {
        SHELF_COLUMNS.forEach { column ->
            addColumn(driver, table = "user_shelf", columnSql = column)
        }
        executeIgnoringExisting(driver, SHELF_KEY_BACKFILL)
        executeIgnoringExisting(driver, SHELF_KEY_DEDUPE)
        executeIgnoringExisting(driver, SHELF_KEY_UNIQUE_INDEX)
    }

    private fun addColumn(driver: SqlDriver, table: String, columnSql: String) {
        executeIgnoringExisting(driver, "ALTER TABLE $table ADD COLUMN $columnSql")
    }

    private fun executeIgnoringExisting(driver: SqlDriver, sql: String) {
        try {
            driver.execute(identifier = null, sql = sql, parameters = 0)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Already applied on databases created from the current schema.
        }
    }

    private val PROFILE_COLUMNS: List<String> = listOf(
        "eu_allergens INTEGER NOT NULL DEFAULT 0",
        "children_caution INTEGER NOT NULL DEFAULT 0",
        "alcohol_leave_on INTEGER NOT NULL DEFAULT 0",
        "essential_oil_cluster INTEGER NOT NULL DEFAULT 0",
        "ads_removed INTEGER NOT NULL DEFAULT 0"
    )

    private val HISTORY_COLUMNS: List<String> = listOf(
        "name TEXT",
        "brand TEXT",
        "usage TEXT NOT NULL DEFAULT 'UNKNOWN'",
        "category TEXT"
    )

    private val SHELF_COLUMNS: List<String> = listOf(
        "shelf_key TEXT",
        "name TEXT",
        "brand TEXT",
        "rating TEXT NOT NULL DEFAULT ''",
        "usage TEXT NOT NULL DEFAULT 'UNKNOWN'",
        "category TEXT"
    )

    private const val CACHED_PRODUCT_TABLE: String =
        """
        CREATE TABLE IF NOT EXISTS cached_online_product (
            gtin TEXT NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            brand TEXT,
            inci_raw TEXT NOT NULL,
            usage TEXT NOT NULL,
            source TEXT NOT NULL,
            cached_at TEXT NOT NULL
        )
        """

    private const val SHELF_KEY_BACKFILL: String =
        """
        UPDATE user_shelf
        SET shelf_key = CASE
            WHEN gtin IS NOT NULL AND gtin != '' THEN 'gtin:' || gtin
            WHEN product_id IS NOT NULL AND product_id != '' THEN 'id:' || product_id
            ELSE 'inci:' || inci_raw
        END
        WHERE shelf_key IS NULL OR shelf_key = ''
        """

    private const val SHELF_KEY_DEDUPE: String =
        """
        DELETE FROM user_shelf
        WHERE rowid NOT IN (
            SELECT MIN(rowid) FROM user_shelf GROUP BY shelf_key
        )
        """

    private const val SHELF_KEY_UNIQUE_INDEX: String =
        "CREATE UNIQUE INDEX IF NOT EXISTS user_shelf_key_idx ON user_shelf(shelf_key)"
}
