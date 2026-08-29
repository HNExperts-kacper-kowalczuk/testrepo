package com.hnexperts.cosmetics.data

import com.hnexperts.cosmetics.catalog.data.CatalogWriter
import com.hnexperts.cosmetics.catalog.overlay.PolishProductOverlay

class CatalogSeeder(
    private val writer: CatalogWriter
) {
    fun seedIfEmpty() {
        writer.seedFromFixturesIfNeeded()
        writer.applyProductOverlay(PolishProductOverlay.products)
    }
}
