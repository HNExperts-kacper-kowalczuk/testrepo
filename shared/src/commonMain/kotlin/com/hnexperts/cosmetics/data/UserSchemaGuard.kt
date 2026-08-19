package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver

object UserSchemaGuard {
    fun ensure(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = CACHED_PRODUCT_TABLE,
            parameters = 0
        )
    }

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
}
