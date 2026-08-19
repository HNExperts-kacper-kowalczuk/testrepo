package com.hnexperts.cosmetics.catalog

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

actual object GzipCodec {
    actual fun inflate(input: ByteArray): ByteArray {
        return GZIPInputStream(ByteArrayInputStream(input)).use { stream -> stream.readBytes() }
    }
}
