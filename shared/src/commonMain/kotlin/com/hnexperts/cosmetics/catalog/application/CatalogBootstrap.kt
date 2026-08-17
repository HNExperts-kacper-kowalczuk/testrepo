package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogBootstrap(
    catalogDatabase: CatalogDatabase,
    dispatchers: AppDispatchers,
    applicationScope: ApplicationScope
) {
    private val ready: CompletableDeferred<CatalogIndex> = CompletableDeferred()

    init {
        applicationScope.coroutineScope.launch {
            try {
                val snapshot: CatalogSnapshot = withContext(dispatchers.catalogDatabase) {
                    CatalogSeeder(catalogDatabase).seedIfEmpty()
                    CatalogIndex.read(catalogDatabase)
                }
                ready.complete(withContext(dispatchers.computation) {
                    CatalogIndex.assemble(snapshot)
                })
            } catch (error: Throwable) {
                ready.completeExceptionally(error)
            }
        }
    }

    suspend fun awaitIndex(): CatalogIndex {
        return ready.await()
    }
}
