package com.hnexperts.cosmetics.data

import android.content.Context
import android.database.Cursor
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
        return AndroidSqliteDriver(
            schema = CatalogDatabase.Schema,
            context = context,
            name = "catalog.db",
            callback = PackedCatalogCallback()
        )
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

private class PackedCatalogCallback : AndroidSqliteDriver.Callback(CatalogDatabase.Schema) {
    override fun onCreate(db: SupportSQLiteDatabase) {
        if (hasCatalogMeta(db)) {
            return
        }
        super.onCreate(db)
    }

    private fun hasCatalogMeta(db: SupportSQLiteDatabase): Boolean {
        val cursor: Cursor = db.query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'catalog_meta' LIMIT 1"
        )
        return cursor.use { rows: Cursor ->
            rows.moveToFirst()
        }
    }
}
