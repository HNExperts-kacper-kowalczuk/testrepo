package com.hnexperts.cosmetics.scanning.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import kotlin.time.Clock
import kotlinx.coroutines.withContext

class SqlReportQueue(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : ReportQueue {
    override suspend fun enqueue(report: CatalogReport): Outcome<Unit> {
        return FailureCatcher.database("report.enqueue") {
            withContext(dispatchers.userDatabase) {
                if (report.gtin != null) {
                    val existing = database.userDatabaseQueries
                        .selectOpenReport(report.gtin, report.kind)
                        .executeAsOneOrNull()
                    if (existing != null) {
                        return@withContext
                    }
                }
                database.userDatabaseQueries.insertReport(
                    created_at = Clock.System.now().toString(),
                    kind = report.kind,
                    gtin = report.gtin,
                    payload_json = report.payloadJson
                )
            }
        }
    }

    override suspend fun attachPayload(gtin: String, kind: String, payloadJson: String): Outcome<Unit> {
        return FailureCatcher.database("report.attach") {
            withContext(dispatchers.userDatabase) {
                val existing = database.userDatabaseQueries.selectOpenReport(gtin, kind).executeAsOneOrNull()
                if (existing != null) {
                    database.userDatabaseQueries.updateReportPayload(payloadJson, existing.id)
                }
            }
        }
    }

    override suspend fun openCount(): Outcome<Long> {
        return FailureCatcher.database("report.count") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.countOpenReports().executeAsOne()
            }
        }
    }

    override suspend fun openReports(): Outcome<List<CatalogReport>> {
        return FailureCatcher.database("report.list") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.selectOpenReports().executeAsList().map { row ->
                    CatalogReport(
                        kind = row.kind,
                        gtin = row.gtin,
                        payloadJson = row.payload_json
                    )
                }
            }
        }
    }

    override suspend fun clear(): Outcome<Unit> {
        return FailureCatcher.database("report.clear") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.clearReports()
            }
        }
    }
}
