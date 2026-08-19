package com.hnexperts.cosmetics.evaluation.application

import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.logging.AppLog
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class EvaluateProduct(
    private val catalog: CatalogGateway,
    private val preferences: PreferencesStore,
    private val history: ScanHistoryRepository,
    private val session: EvaluationSession,
    private val dispatchers: AppDispatchers
) {
    suspend fun invoke(
        inciRaw: String,
        source: String,
        productName: String? = null,
        brand: String? = null,
        gtin: String? = null,
        usage: ProductUsage = ProductUsage.UNKNOWN,
        packVerified: Boolean = false,
        category: String? = null,
        productId: String? = null
    ): Outcome<ProductAssessment> {
        val inputs: EvaluationInputs = when (val loaded: Outcome<EvaluationInputs> = loadInputs()) {
            is Outcome.Err -> return loaded
            is Outcome.Ok -> loaded.value
        }
        val scored: Outcome<ProductAssessment> = score(
            index = inputs.index,
            profile = inputs.profile,
            inciRaw = inciRaw,
            productName = productName,
            brand = brand,
            gtin = gtin,
            usage = usage
        )
        val assessment: ProductAssessment = when (scored) {
            is Outcome.Err -> return scored
            is Outcome.Ok -> scored.value.copy(
                packVerified = packVerified,
                category = category,
                productId = productId
            )
        }
        persist(assessment, source)
        return Outcome.Ok(assessment)
    }

    private suspend fun loadInputs(): Outcome<EvaluationInputs> {
        return coroutineScope {
            val indexDeferred = async { catalog.awaitIndex() }
            val profileDeferred = async { preferences.load() }
            val zipped: Outcome<Pair<CatalogIndex, StoredPreferences>> =
                Outcome.zip(indexDeferred.await(), profileDeferred.await())
            when (zipped) {
                is Outcome.Err -> zipped
                is Outcome.Ok -> Outcome.Ok(
                    EvaluationInputs(
                        index = zipped.value.first,
                        profile = zipped.value.second.profile
                    )
                )
            }
        }
    }

    private suspend fun score(
        index: CatalogIndex,
        profile: UserAvoidanceProfile,
        inciRaw: String,
        productName: String?,
        brand: String?,
        gtin: String?,
        usage: ProductUsage
    ): Outcome<ProductAssessment> {
        return FailureCatcher.evaluation("evaluation.score") {
            withContext(dispatchers.computation) {
                index.evaluateFormula.evaluateAsync(
                    inciRaw = inciRaw,
                    profile = profile,
                    productName = productName,
                    brand = brand,
                    gtin = gtin,
                    usage = usage
                )
            }
        }
    }

    private suspend fun persist(assessment: ProductAssessment, source: String) {
        session.publish(assessment, source)
        when (val recorded: Outcome<Unit> = history.record(assessment, source)) {
            is Outcome.Ok -> Unit
            is Outcome.Err -> AppLog.w("evaluation.persist", recorded.failure.verboseMessage())
        }
    }

    private data class EvaluationInputs(
        val index: CatalogIndex,
        val profile: UserAvoidanceProfile
    )
}
