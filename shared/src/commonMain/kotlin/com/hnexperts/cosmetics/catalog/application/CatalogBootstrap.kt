package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.data.CatalogSnapshotReader
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogBootstrap(
    private val seeder: CatalogSeeder,
    private val snapshotReader: CatalogSnapshotReader,
    private val dispatchers: AppDispatchers,
    applicationScope: ApplicationScope
) : CatalogGateway {
    private val ready: CompletableDeferred<Outcome<CatalogIndex>> = CompletableDeferred()

    init {
        applicationScope.coroutineScope.launch {
            val outcome: Outcome<CatalogIndex> = FailureCatcher.catalog("catalog.bootstrap") {
                val snapshot: CatalogSnapshot = withContext(dispatchers.catalogDatabase) {
                    seeder.seedIfEmpty()
                    snapshotReader.read()
                }
                withContext(dispatchers.computation) {
                    CatalogIndex.assemble(snapshot)
                }
            }
            if (outcome is Outcome.Err) {
                AppLog.e("catalog.bootstrap", outcome.failure.verboseMessage())
            }
            ready.complete(outcome)
        }
    }

    override suspend fun awaitIndex(): Outcome<CatalogIndex> {
        return ready.await()
    }
}
