package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.application.CatalogBootstrap
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.data.SqlHistoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
        val inputs: EvaluationInputs = loadInputs()
        val assessment: ProductAssessment = score(
            index = inputs.index,
            profile = inputs.profile,
            inciRaw = inciRaw,
            productName = productName,
            brand = brand,
            gtin = gtin
        )
        persist(assessment, source)
        return assessment
    }

    private suspend fun loadInputs(): EvaluationInputs {
        return coroutineScope {
            val indexDeferred = async { catalog.awaitIndex() }
            val profileDeferred = async { preferences.load().profile }
            EvaluationInputs(
                index = indexDeferred.await(),
                profile = profileDeferred.await()
            )
        }
    }

    private suspend fun score(
        index: CatalogIndex,
        profile: UserAvoidanceProfile,
        inciRaw: String,
        productName: String?,
        brand: String?,
        gtin: String?
    ): ProductAssessment {
        return withContext(dispatchers.computation) {
            index.evaluateFormula.evaluateAsync(
                inciRaw = inciRaw,
                profile = profile,
                productName = productName,
                brand = brand,
                gtin = gtin
            )
        }
    }

    private suspend fun persist(assessment: ProductAssessment, source: String) {
        coroutineScope {
            launch { session.publish(assessment, source) }
            launch { history.record(assessment, source) }
        }
    }

    private data class EvaluationInputs(
        val index: CatalogIndex,
        val profile: UserAvoidanceProfile
    )
}
