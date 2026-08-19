package com.hnexperts.cosmetics.catalog

import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class GzipCodecTest {
    @Test
    fun inflatesGzipBytes() {
        val original: ByteArray = "catalog-bytes".encodeToByteArray()
        val compressed = ByteArrayOutputStream().use { out ->
            GZIPOutputStream(out).use { gzip -> gzip.write(original) }
            out.toByteArray()
        }
        assertEquals("catalog-bytes", GzipCodec.inflate(compressed).decodeToString())
    }
}
