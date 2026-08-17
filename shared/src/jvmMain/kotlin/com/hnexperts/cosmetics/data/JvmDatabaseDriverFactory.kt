package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createCatalogDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CatalogDatabase.Schema.create(driver)
        return driver
    }

    override fun createUserDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        UserDatabase.Schema.create(driver)
        return driver
    }
}
