package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment

data class CompareCandidate(
    val inciRaw: String,
    val productName: String?,
    val brand: String?,
    val gtin: String?,
    val usage: ProductUsage,
    val productId: String? = null,
    val category: String? = null
)

class CompareSession {
    private var candidates: List<CompareCandidate> = emptyList()
    private var unnamedFormat: String = DEFAULT_UNNAMED_FORMAT

    fun publish(next: List<CompareCandidate>, unnamedFormat: String = DEFAULT_UNNAMED_FORMAT) {
        candidates = next
        this.unnamedFormat = unnamedFormat.ifBlank { DEFAULT_UNNAMED_FORMAT }
    }

    fun current(): List<CompareCandidate> {
        return candidates
    }

    fun unnamedFormat(): String {
        return unnamedFormat
    }

    companion object {
        const val DEFAULT_UNNAMED_FORMAT: String = "Product {n}"
    }
}
