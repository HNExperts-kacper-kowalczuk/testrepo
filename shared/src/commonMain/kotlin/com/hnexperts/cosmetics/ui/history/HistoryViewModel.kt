package com.hnexperts.cosmetics.ui.history

import androidx.lifecycle.ViewModel
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.scanning.data.HistoryEntry
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository

class HistoryViewModel(
    private val history: SqlHistoryRepository,
    private val index: CatalogIndex,
    private val session: EvaluationSession,
    private val preferences: SqlPreferencesRepository
) : ViewModel() {
    fun entries(): List<HistoryEntry> {
        return history.recent()
    }

    fun reopen(entry: HistoryEntry) {
        val assessment = index.evaluateFormula.evaluate(
            inciRaw = entry.inciRaw,
            profile = preferences.load().profile,
            productName = null,
            brand = null,
            gtin = entry.gtin
        )
        session.lastAssessment = assessment
        session.lastSource = entry.source
    }
}
