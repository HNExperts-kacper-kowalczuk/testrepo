package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import com.hnexperts.cosmetics.catalog.application.IosBundledCatalog
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createCatalogDriver(): SqlDriver {
        val basePath: String = IosBundledCatalog.install()
        return NativeSqliteDriver(
            schema = CatalogDatabase.Schema,
            name = "catalog.db",
            onConfiguration = { config: DatabaseConfiguration ->
                config.copy(
                    extendedConfig = config.extendedConfig.copy(basePath = basePath)
                )
            }
        )
    }

    override fun createUserDriver(): SqlDriver {
        val driver: SqlDriver = NativeSqliteDriver(UserDatabase.Schema, "user.db")
        UserSchemaGuard.ensure(driver)
        return driver
    }
}
