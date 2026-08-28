package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import kotlinx.coroutines.withContext

class ApplyCatalogDelta(
    private val mutations: CatalogMutationStore,
    private val deltas: CatalogDeltaSource,
    private val catalog: CatalogGateway,
    private val dispatchers: AppDispatchers
) {
    suspend fun invoke(published: CatalogManifest): Outcome<CatalogIndex> {
        val ready: Outcome<CatalogIndex> = catalog.awaitIndex()
        val local: CatalogIndex = when (ready) {
            is Outcome.Err -> return ready
            is Outcome.Ok -> ready.value
        }
        val applied: Outcome<Unit> = FailureCatcher.catalog("catalog.sync.apply") {
            val delta: CatalogDelta = deltas.deltaFor(
                fromVersion = local.meta.catalogVersion,
                published = published
            ) ?: throw NoCatalogDeltaException(published.catalogVersion)
            withContext(dispatchers.catalogDatabase) {
                mutations.applyDelta(delta)
            }
        }
        return when (applied) {
            is Outcome.Err -> applied
            is Outcome.Ok -> catalog.reload()
        }
    }
}

class NoCatalogDeltaException(
    toVersion: String
) : IllegalStateException("No bundled catalog delta for version $toVersion")
