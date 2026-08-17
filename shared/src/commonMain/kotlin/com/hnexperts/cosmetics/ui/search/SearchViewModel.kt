package com.hnexperts.cosmetics.ui.search

import androidx.lifecycle.ViewModel
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository

class SearchViewModel(
    private val products: SqlProductRepository,
    private val index: CatalogIndex,
    private val session: EvaluationSession,
    private val preferences: SqlPreferencesRepository,
    private val history: SqlHistoryRepository
) : ViewModel() {
    fun query(text: String): List<Product> {
        return products.search(text)
    }

    fun openProduct(product: Product) {
        val assessment = index.evaluateFormula.evaluate(
            inciRaw = product.inciRaw,
            profile = preferences.load().profile,
            productName = product.name,
            brand = product.brand,
            gtin = null
        )
        session.lastAssessment = assessment
        session.lastSource = "search"
        history.record(assessment, "search")
    }
}
