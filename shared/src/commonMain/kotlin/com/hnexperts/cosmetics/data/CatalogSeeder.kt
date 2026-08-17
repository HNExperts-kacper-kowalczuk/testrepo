package com.hnexperts.cosmetics.data

import com.hnexperts.cosmetics.catalog.data.CatalogWriter

class CatalogSeeder(
    private val writer: CatalogWriter
) {
    fun seedIfEmpty() {
        writer.seedFromFixturesIfNeeded()
    }
}
