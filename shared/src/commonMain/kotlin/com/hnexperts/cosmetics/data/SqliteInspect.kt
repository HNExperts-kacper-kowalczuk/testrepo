package com.hnexperts.cosmetics.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver

internal object SqliteInspect {
    fun tableExists(driver: SqlDriver, table: String): Boolean {
        requireIdent(table)
        return driver.executeQuery(
            identifier = null,
            sql = "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$table' LIMIT 1",
            mapper = { cursor -> QueryResult.Value(cursor.next().value) },
            parameters = 0
        ).value
    }

    fun columnExists(driver: SqlDriver, table: String, column: String): Boolean {
        return columns(driver, table).contains(column)
    }

    fun columns(driver: SqlDriver, table: String): Set<String> {
        requireIdent(table)
        return driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info($table)",
            mapper = { cursor -> QueryResult.Value(readColumnNames(cursor)) },
            parameters = 0
        ).value
    }

    private fun readColumnNames(cursor: SqlCursor): Set<String> {
        val names: MutableSet<String> = mutableSetOf()
        while (cursor.next().value) {
            addName(names, cursor.getString(1))
        }
        return names
    }

    private fun addName(names: MutableSet<String>, name: String?) {
        if (name != null) {
            names.add(name)
        }
    }

    private fun requireIdent(name: String) {
        require(IDENT.matches(name)) { "invalid sqlite identifier: $name" }
    }

    private val IDENT: Regex = Regex("[a-z][a-z0-9_]*")
}
