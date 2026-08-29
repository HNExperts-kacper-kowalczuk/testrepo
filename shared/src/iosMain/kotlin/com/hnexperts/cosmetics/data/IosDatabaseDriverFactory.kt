package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.DatabaseConnection
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
                packedCatalogConfiguration(config, basePath)
            }
        )
    }

    override fun createUserDriver(): SqlDriver {
        val driver: SqlDriver = NativeSqliteDriver(UserDatabase.Schema, "user.db")
        UserSchemaGuard.ensure(driver)
        return driver
    }
}

private fun packedCatalogConfiguration(
    config: DatabaseConfiguration,
    basePath: String
): DatabaseConfiguration {
    return config.copy(
        extendedConfig = config.extendedConfig.copy(basePath = basePath),
        create = ::createPackedCatalogIfEmpty
    )
}

private fun createPackedCatalogIfEmpty(connection: DatabaseConnection) {
    wrapConnection(connection) { driver: SqlDriver ->
        if (!SqliteInspect.tableExists(driver, "catalog_meta")) {
            CatalogDatabase.Schema.create(driver)
        }
    }
}
