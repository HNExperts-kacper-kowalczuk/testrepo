package com.hnexperts.cosmetics.catalog.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GtinNormalizerTest {
    @Test
    fun padsUpcAToEan13() {
        assertEquals("0590123412345", GtinNormalizer.normalize("590123412345"))
    }

    @Test
    fun stripsNonDigits() {
        assertEquals("5901234123457", GtinNormalizer.normalize("590-1234-12345-7"))
    }

    @Test
    fun detectsGs1PolandPrefix() {
        assertTrue(GtinNormalizer.isGs1Poland("5900017071398"))
        assertTrue(GtinNormalizer.isGs1Poland("590-1887-019367"))
        assertFalse(GtinNormalizer.isGs1Poland("4000000000001"))
    }
}
