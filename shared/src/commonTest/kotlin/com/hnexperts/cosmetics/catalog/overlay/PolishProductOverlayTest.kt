package com.hnexperts.cosmetics.catalog.overlay

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PolishProductOverlayTest {
    @Test
    fun includesTheRequestedPolishGtinsWithInci() {
        val bambino = product("5900017071398")
        val ziaja = product("5901887019367")
        assertEquals("Bambino", bambino.product.brand)
        assertTrue(bambino.product.inciRaw.startsWith("Aqua"))
        assertEquals("Ziaja", ziaja.product.brand)
        assertTrue(ziaja.product.verified)
        assertTrue(ziaja.product.inciRaw.contains("Aluminum Chlorohydrate"))
    }

    @Test
    fun includesManufacturerPublishedPolishGtins() {
        assertTrue(PolishProductOverlay.products.size >= 8)
        assertNotNull(product("5901887049449"))
        assertNotNull(product("5901887007210"))
        assertNotNull(product("5901887009924"))
        val mint = product("5901887049449")
        assertEquals("Ziaja", mint.product.brand)
        assertTrue(mint.product.verified)
        assertTrue(mint.product.inciRaw.contains("Decyl Glucoside"))
    }

    @Test
    fun everyOverlayRowHasAUsableInciAndGs1PolandGtin() {
        assertTrue(PolishProductOverlay.dump.region == "PL")
        assertTrue(PolishProductOverlay.products.isNotEmpty())
        for (item in PolishProductOverlay.products) {
            assertTrue(item.product.inciRaw.length >= 20, item.product.id)
            assertTrue(item.gtins.all { gtin -> gtin.startsWith("590") }, item.product.id)
        }
    }

    private fun product(gtin: String) = assertNotNull(
        PolishProductOverlay.products.firstOrNull { item -> item.gtins.contains(gtin) }
    )
}
