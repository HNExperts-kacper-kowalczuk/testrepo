package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.catalog.fixture.EvaluationFactory
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SummarizeScanHazardsTest {
    private val summarize: SummarizeScanHazards = SummarizeScanHazards()
    private val evaluateFormula: EvaluateFormula = EvaluationFactory.create()

    @Test
    fun formaldehydeLeadsWhenItAppearsInTwoScans() {
        val entries: List<HistoryEntry> = listOf(
            history(id = 1L, inciRaw = "Aqua, Formaldehyde, Glycerin"),
            history(id = 2L, inciRaw = "Aqua, Formaldehyde, Glycerin"),
            history(id = 3L, inciRaw = "Aqua, Glycerin")
        )
        val names: List<String> = summarize.invoke(entries, evaluateFormula, UserAvoidanceProfile.EMPTY)
        assertEquals("Formaldehyde", names.first())
        assertTrue(names.size <= 5)
    }

    @Test
    fun emptyWhenFewerThanTwoScans() {
        val names: List<String> = summarize.invoke(
            entries = listOf(history(id = 1L, inciRaw = "Aqua, Formaldehyde, Glycerin")),
            evaluateFormula = evaluateFormula,
            profile = UserAvoidanceProfile.EMPTY
        )
        assertTrue(names.isEmpty())
    }

    @Test
    fun emptyWhenNoHighOrProhibitedHits() {
        val names: List<String> = summarize.invoke(
            entries = listOf(
                history(id = 1L, inciRaw = "Aqua, Glycerin"),
                history(id = 2L, inciRaw = "Aqua, Niacinamide, Glycerin")
            ),
            evaluateFormula = evaluateFormula,
            profile = UserAvoidanceProfile.EMPTY
        )
        assertTrue(names.isEmpty())
    }

    private fun history(id: Long, inciRaw: String): HistoryEntry {
        return HistoryEntry(
            id = id,
            scannedAt = "2026-01-01T00:00:00Z",
            gtin = null,
            productId = null,
            inciRaw = inciRaw,
            rating = "HIGH",
            source = "manual",
            usage = ProductUsage.LEAVE_ON
        )
    }
}
