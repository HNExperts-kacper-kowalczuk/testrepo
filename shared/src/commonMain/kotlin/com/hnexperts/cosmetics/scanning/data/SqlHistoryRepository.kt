package com.hnexperts.cosmetics.scanning.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import kotlinx.coroutines.withContext

class SqlHistoryRepository(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : ScanHistoryRepository {
    override suspend fun record(assessment: ProductAssessment, source: String): Outcome<Unit> {
        return FailureCatcher.database("history.record") {
            withContext(dispatchers.userDatabase) {
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
    }

    override suspend fun recent(): Outcome<List<HistoryEntry>> {
        return FailureCatcher.database("history.recent") {
            withContext(dispatchers.userDatabase) {
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

    override suspend fun clear(): Outcome<Unit> {
        return FailureCatcher.database("history.clear") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.clearHistory()
            }
        }
    }
}
