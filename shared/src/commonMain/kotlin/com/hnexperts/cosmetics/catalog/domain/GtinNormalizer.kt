package com.hnexperts.cosmetics.catalog.domain

object GtinNormalizer {
    fun normalize(raw: String): String {
        val digits: String = raw.filter { character -> character.isDigit() }
        if (digits.length == 12) {
            return "0$digits"
        }
        return digits
    }
}
