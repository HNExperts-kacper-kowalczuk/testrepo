package com.hnexperts.cosmetics.scanning.application

import com.hnexperts.cosmetics.crypto.Sha256
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FlushReports(
    private val reports: ReportQueue,
    private val http: SimpleHttpClient,
    private val reportsUrl: String
) {
    fun isConfigured(): Boolean {
        return reportsUrl.isNotBlank()
    }

    suspend fun invoke(): Outcome<Unit> {
        if (!isConfigured()) {
            return Outcome.Err(
                AppFailure.Network(operation = "report.flush", detail = "Reports URL is not configured")
            )
        }
        val open: List<CatalogReport> = when (val listed: Outcome<List<CatalogReport>> = reports.openReports()) {
            is Outcome.Ok -> listed.value
            is Outcome.Err -> return listed
        }
        if (open.isEmpty()) {
            return Outcome.Ok(Unit)
        }
        val posted: Outcome<Unit> = http.postJson(reportsUrl, ReportFlushCodec.encode(open))
        return when (posted) {
            is Outcome.Err -> posted
            is Outcome.Ok -> reports.markAllOpenFlushed()
        }
    }
}

object ReportFlushCodec {
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encode(reports: List<CatalogReport>): String {
        val body = ReportFlushRequest(
            reports = reports.map { report ->
                ReportFlushRow(
                    kind = report.kind,
                    gtin = report.gtin,
                    payloadSha256 = Sha256.hex(report.payloadJson)
                )
            }
        )
        return json.encodeToString(ReportFlushRequest.serializer(), body)
    }
}

@Serializable
data class ReportFlushRequest(
    val reports: List<ReportFlushRow>
)

@Serializable
data class ReportFlushRow(
    val kind: String,
    val gtin: String? = null,
    val payloadSha256: String
)
