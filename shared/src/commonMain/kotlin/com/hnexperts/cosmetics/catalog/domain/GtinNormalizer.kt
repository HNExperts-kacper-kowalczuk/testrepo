package com.hnexperts.cosmetics.catalog.domain

object GtinNormalizer {
    fun normalize(raw: String): String {
        val digits: String = raw.filter { character -> character.isDigit() }
        if (digits.length == 12) {
            return "0$digits"
        }
        return digits
    }

    fun isGs1Poland(raw: String): Boolean {
        return normalize(raw).startsWith(GS1_POLAND_PREFIX)
    }

    private const val GS1_POLAND_PREFIX: String = "590"
}
