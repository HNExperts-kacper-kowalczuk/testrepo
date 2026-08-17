package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.application.CatalogBootstrap
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository
import kotlinx.coroutines.withContext

class EvaluateProduct(
    private val catalog: CatalogBootstrap,
    private val preferences: SqlPreferencesRepository,
    private val history: SqlHistoryRepository,
    private val session: EvaluationSession,
    private val dispatchers: AppDispatchers
) {
    suspend fun invoke(
        inciRaw: String,
        source: String,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null
    ): ProductAssessment {
        val index = catalog.awaitIndex()
        val profile = preferences.load().profile
        val assessment: ProductAssessment = withContext(dispatchers.computation) {
            index.evaluateFormula.evaluateAsync(
                inciRaw = inciRaw,
                profile = profile,
                productName = productName,
                brand = brand,
                gtin = gtin
            )
        }
        session.publish(assessment, source)
        history.record(assessment, source)
        return assessment
    }
}
