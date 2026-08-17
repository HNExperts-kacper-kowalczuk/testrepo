package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.data.catalogdb.CatalogDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CatalogBootstrap(
    catalogDatabase: CatalogDatabase,
    dispatchers: AppDispatchers
) {
    private val ready: CompletableDeferred<CatalogIndex> = CompletableDeferred()

    init {
        CoroutineScope(SupervisorJob() + dispatchers.database).launch {
            try {
                CatalogSeeder(catalogDatabase).seedIfEmpty()
                ready.complete(CatalogIndex.load(catalogDatabase))
            } catch (error: Throwable) {
                ready.completeExceptionally(error)
            }
        }
    }

    suspend fun awaitIndex(): CatalogIndex {
        return ready.await()
    }
}
