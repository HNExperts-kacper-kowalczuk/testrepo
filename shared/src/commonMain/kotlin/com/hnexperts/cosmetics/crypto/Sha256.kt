package com.hnexperts.cosmetics.crypto

expect object Sha256 {
    fun hex(text: String): String
}
