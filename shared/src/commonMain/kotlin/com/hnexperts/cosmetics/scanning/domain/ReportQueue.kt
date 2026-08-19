package com.hnexperts.cosmetics.scanning.domain

import com.hnexperts.cosmetics.failure.Outcome

data class CatalogReport(
    val kind: String,
    val gtin: String?,
    val payloadJson: String
)

interface ReportQueue {
    suspend fun enqueue(report: CatalogReport): Outcome<Unit>
    suspend fun attachPayload(gtin: String, kind: String, payloadJson: String): Outcome<Unit>
    suspend fun openCount(): Outcome<Long>
}

object ReportKinds {
    const val MISSING_PRODUCT: String = "missing_product"
    const val WRONG_INCI: String = "wrong_inci"
}
