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

    fun publish(next: List<CompareCandidate>) {
        candidates = next
    }

    fun current(): List<CompareCandidate> {
        return candidates
    }
}
