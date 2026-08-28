package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.crypto.Sha256
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.ReportKinds
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class FlushReportsTest {
    @Test
    fun successfulPostMarksOpenRowsFlushedAndSendsHashesOnly() = runBlocking {
        val reports = MemoryReports()
        reports.items += CatalogReport(
            kind = ReportKinds.WRONG_INCI,
            gtin = "5901234123457",
            payloadJson = "secret-inci-list"
        )
        val http = RecordingPostHttp()
        val flush = FlushReports(reports, http, "https://reports.example.test/flush")
        val outcome: Outcome<Unit> = flush.invoke()
        assertIs<Outcome.Ok<Unit>>(outcome)
        assertTrue(reports.items.isEmpty())
        val body: String = http.postedBody.orEmpty()
        assertTrue(body.contains(Sha256.hex("secret-inci-list")))
        assertFalse(body.contains("secret-inci-list"))
        assertTrue(body.contains(ReportKinds.WRONG_INCI))
        assertTrue(body.contains("5901234123457"))
    }

    @Test
    fun http500LeavesOpenRows() = runBlocking {
        val reports = MemoryReports()
        reports.items += CatalogReport(kind = ReportKinds.MISSING_PRODUCT, gtin = "1", payloadJson = "{}")
        val http = RecordingPostHttp(status = 500)
        val flush = FlushReports(reports, http, "https://reports.example.test/flush")
        val outcome: Outcome<Unit> = flush.invoke()
        assertIs<Outcome.Err>(outcome)
        assertEquals(1, reports.items.size)
    }

    @Test
    fun unconfiguredUrlDoesNotPost() = runBlocking {
        val reports = MemoryReports()
        reports.items += CatalogReport(kind = ReportKinds.MISSING_PRODUCT, gtin = "1", payloadJson = "{}")
        val http = RecordingPostHttp()
        val flush = FlushReports(reports, http, "")
        assertFalse(flush.isConfigured())
        val outcome: Outcome<Unit> = flush.invoke()
        assertIs<Outcome.Err>(outcome)
        assertEquals(null, http.postedBody)
        assertEquals(1, reports.items.size)
    }

    private class MemoryReports : ReportQueue {
        val items: MutableList<CatalogReport> = mutableListOf()

        override suspend fun enqueue(report: CatalogReport): Outcome<Unit> {
            items += report
            return Outcome.Ok(Unit)
        }

        override suspend fun attachPayload(gtin: String, kind: String, payloadJson: String): Outcome<Unit> {
            return Outcome.Ok(Unit)
        }

        override suspend fun openCount(): Outcome<Long> {
            return Outcome.Ok(items.size.toLong())
        }

        override suspend fun openReports(): Outcome<List<CatalogReport>> {
            return Outcome.Ok(items.toList())
        }

        override suspend fun markAllOpenFlushed(): Outcome<Unit> {
            items.clear()
            return Outcome.Ok(Unit)
        }

        override suspend fun clear(): Outcome<Unit> {
            items.clear()
            return Outcome.Ok(Unit)
        }
    }

    private class RecordingPostHttp(
        private val status: Int = 200
    ) : SimpleHttpClient {
        var postedBody: String? = null

        override suspend fun getText(url: String): Outcome<String> {
            return Outcome.Err(AppFailure.Network(operation = "http.get", detail = "unused"))
        }

        override suspend fun postJson(url: String, body: String): Outcome<Unit> {
            postedBody = body
            if (status !in 200..299) {
                return Outcome.Err(AppFailure.Network(operation = "http.post", detail = "HTTP $status"))
            }
            return Outcome.Ok(Unit)
        }
    }
}
