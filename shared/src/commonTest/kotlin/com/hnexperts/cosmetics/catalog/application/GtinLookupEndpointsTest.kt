package com.hnexperts.cosmetics.catalog.application

import kotlin.test.Test
import kotlin.test.assertEquals

class GtinLookupEndpointsTest {
    @Test
    fun gs1PolandTriesPolishBeautyFactsFirstThenWorldThenProductsFacts() {
        val urls: List<String> = GtinLookupEndpoints.productJsonUrls("5900017071398")
        assertEquals(
            listOf(
                "https://pl.openbeautyfacts.org/api/v2/product/5900017071398.json",
                "https://world.openbeautyfacts.org/api/v2/product/5900017071398.json",
                "https://pl.openfoodfacts.org/api/v2/product/5900017071398.json",
                "https://world.openfoodfacts.org/api/v2/product/5900017071398.json",
                "https://world.openproductsfacts.org/api/v2/product/5900017071398.json"
            ),
            urls
        )
    }

    @Test
    fun otherPrefixesSkipThePolishMirrors() {
        val urls: List<String> = GtinLookupEndpoints.productJsonUrls("4000000000001")
        assertEquals(
            listOf(
                "https://world.openbeautyfacts.org/api/v2/product/4000000000001.json",
                "https://world.openfoodfacts.org/api/v2/product/4000000000001.json",
                "https://world.openproductsfacts.org/api/v2/product/4000000000001.json"
            ),
            urls
        )
    }
}
