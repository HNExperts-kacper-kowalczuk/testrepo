package com.hnexperts.cosmetics.catalog.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class GtinNormalizerTest {
    @Test
    fun padsUpcAToEan13() {
        assertEquals("0590123412345", GtinNormalizer.normalize("590123412345"))
    }

    @Test
    fun stripsNonDigits() {
        assertEquals("5901234123457", GtinNormalizer.normalize("590-1234-12345-7"))
    }
}
