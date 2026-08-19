package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.Outcome

data class HistoryEntry(
    val id: Long,
    val scannedAt: String,
    val gtin: String?,
    val productId: String?,
    val inciRaw: String,
    val rating: String,
    val source: String,
    val name: String? = null,
    val brand: String? = null,
    val usage: ProductUsage = ProductUsage.UNKNOWN,
    val category: String? = null
)

interface ScanHistoryRepository {
    suspend fun record(assessment: ProductAssessment, source: String): Outcome<Unit>
    suspend fun recent(): Outcome<List<HistoryEntry>>
    suspend fun clear(): Outcome<Unit>
}
