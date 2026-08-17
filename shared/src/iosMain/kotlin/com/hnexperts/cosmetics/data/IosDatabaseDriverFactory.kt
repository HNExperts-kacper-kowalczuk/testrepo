package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createCatalogDriver(): SqlDriver {
        return NativeSqliteDriver(CatalogDatabase.Schema, "catalog.db")
    }

    override fun createUserDriver(): SqlDriver {
        return NativeSqliteDriver(UserDatabase.Schema, "user.db")
    }
}
