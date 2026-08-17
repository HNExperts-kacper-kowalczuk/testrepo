package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createCatalogDriver(): SqlDriver
    fun createUserDriver(): SqlDriver
}
