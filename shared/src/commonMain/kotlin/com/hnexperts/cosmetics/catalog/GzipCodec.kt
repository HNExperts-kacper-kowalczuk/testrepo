package com.hnexperts.cosmetics.catalog

expect object GzipCodec {
    fun inflate(input: ByteArray): ByteArray
}
