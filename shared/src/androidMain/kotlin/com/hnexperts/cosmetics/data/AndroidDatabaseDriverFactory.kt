package com.hnexperts.cosmetics.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val driver: SqlDriver = AndroidSqliteDriver(
            schema = UserDatabase.Schema,
            context = context,
            name = "user.db",
            callback = AdditiveUserSchemaCallback()
        )
        UserSchemaGuard.ensure(driver)
        return driver
    }
}

private class AdditiveUserSchemaCallback : AndroidSqliteDriver.Callback(UserDatabase.Schema) {
    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // UserSchemaGuard applies additive columns/tables after the driver opens.
    }
}
