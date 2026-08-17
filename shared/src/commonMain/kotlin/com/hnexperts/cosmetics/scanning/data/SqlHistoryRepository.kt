package com.hnexperts.cosmetics.scanning.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import kotlinx.coroutines.withContext

data class HistoryEntry(
    val id: Long,
    val scannedAt: String,
    val gtin: String?,
    val productId: String?,
    val inciRaw: String,
    val rating: String,
    val source: String
)

class SqlHistoryRepository(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) {
    suspend fun record(assessment: ProductAssessment, source: String) {
        withContext(dispatchers.database) {
            database.userDatabaseQueries.insertHistory(
                scanned_at = kotlin.time.Clock.System.now().toString(),
                gtin = assessment.gtin,
                product_id = null,
                inci_raw = assessment.inciRaw,
                rating = assessment.overall.name,
                source = source
            )
        }
    }

    suspend fun recent(): List<HistoryEntry> {
        return withContext(dispatchers.database) {
            database.userDatabaseQueries.selectHistory().executeAsList().map { row ->
                HistoryEntry(
                    id = row.id,
                    scannedAt = row.scanned_at,
                    gtin = row.gtin,
                    productId = row.product_id,
                    inciRaw = row.inci_raw,
                    rating = row.rating,
                    source = row.source
                )
            }
        }
    }
}
