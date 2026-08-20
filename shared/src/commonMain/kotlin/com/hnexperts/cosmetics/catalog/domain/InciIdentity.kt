package com.hnexperts.cosmetics.catalog.domain

import com.hnexperts.cosmetics.crypto.Sha256
import com.hnexperts.cosmetics.ingredients.domain.InciTokenizer

object InciIdentity {
    private val tokenizer: InciTokenizer = InciTokenizer(emptyList())

    fun hash(inciRaw: String): String {
        val tokens: List<String> = tokenizer.tokenize(inciRaw)
        return Sha256.hex(tokens.joinToString(separator = ","))
    }
}
