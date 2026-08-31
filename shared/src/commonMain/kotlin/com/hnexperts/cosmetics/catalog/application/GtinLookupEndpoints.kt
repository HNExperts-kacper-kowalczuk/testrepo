package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.GtinNormalizer

object GtinLookupEndpoints {
    fun productJsonUrls(gtin: String): List<String> {
        val worldBeauty: String = "$WORLD_OBF$gtin.json"
        val worldFood: String = "$WORLD_OFF$gtin.json"
        val worldProducts: String = "$WORLD_OPF$gtin.json"
        if (!GtinNormalizer.isGs1Poland(gtin)) {
            return listOf(worldBeauty, worldFood, worldProducts)
        }
        return listOf(
            "$PL_OBF$gtin.json",
            worldBeauty,
            "$PL_OFF$gtin.json",
            worldFood,
            worldProducts
        )
    }

    private const val WORLD_OBF: String = "https://world.openbeautyfacts.org/api/v2/product/"
    private const val WORLD_OFF: String = "https://world.openfoodfacts.org/api/v2/product/"
    private const val WORLD_OPF: String = "https://world.openproductsfacts.org/api/v2/product/"
    private const val PL_OBF: String = "https://pl.openbeautyfacts.org/api/v2/product/"
    private const val PL_OFF: String = "https://pl.openfoodfacts.org/api/v2/product/"
}
