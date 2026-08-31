package com.hnexperts.cosmetics.scanning.domain

object InciRawEditor {
    fun replaceAt(tokens: List<String>, index: Int, replacement: String): String {
        val updated: List<String> = if (index in tokens.indices) {
            tokens.mapIndexed { position, token ->
                if (position == index) replacement else token
            }
        } else {
            tokens
        }
        return updated.filter { name -> name.isNotBlank() }.joinToString(", ")
    }
}
