package com.hnexperts.cosmetics.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hnexperts.cosmetics.catalog.application.AndroidBundledCatalog
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import com.hnexperts.cosmetics.data.userdb.UserDatabase

class AndroidDatabaseDriverFactory(
    private val context: Context
) : DatabaseDriverFactory {
    override fun createCatalogDriver(): SqlDriver {
        AndroidBundledCatalog.install(context)
        return AndroidSqliteDriver(CatalogDatabase.Schema, context, "catalog.db")
    }

    override fun createUserDriver(): SqlDriver {
        val driver: SqlDriver = AndroidSqliteDriver(UserDatabase.Schema, context, "user.db")
        UserSchemaGuard.ensure(driver)
        return driver
    }
}
