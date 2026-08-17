package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.data.CatalogSnapshotReader
import com.hnexperts.cosmetics.catalog.data.CatalogWriter
import com.hnexperts.cosmetics.catalog.domain.CorruptCatalogException
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.concurrency.ApplicationScope
import com.hnexperts.cosmetics.data.CatalogSeeder
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.logging.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CatalogBootstrap(
    private val seeder: CatalogSeeder,
    private val snapshotReader: CatalogSnapshotReader,
    private val writer: CatalogWriter,
    private val dispatchers: AppDispatchers,
    applicationScope: ApplicationScope
) : CatalogGateway {
    private val mutex: Mutex = Mutex()
    private val first: CompletableDeferred<Outcome<CatalogIndex>> = CompletableDeferred()
    private var latest: Outcome<CatalogIndex>? = null

    init {
        applicationScope.coroutineScope.launch {
            val outcome: Outcome<CatalogIndex> = loadOnce()
            mutex.withLock {
                latest = outcome
                first.complete(outcome)
            }
        }
    }

    override suspend fun awaitIndex(): Outcome<CatalogIndex> {
        first.await()
        return mutex.withLock { requireNotNull(latest) }
    }

    override suspend fun reload(): Outcome<CatalogIndex> {
        first.await()
        return mutex.withLock {
            val outcome: Outcome<CatalogIndex> = loadOnce()
            latest = outcome
            outcome
        }
    }

    private suspend fun loadOnce(): Outcome<CatalogIndex> {
        return FailureCatcher.catalog("catalog.bootstrap") {
            val snapshot: CatalogSnapshot = withContext(dispatchers.catalogDatabase) {
                loadOrRepair()
            }
            withContext(dispatchers.computation) {
                CatalogIndex.assemble(snapshot)
            }
        }
    }

    private fun loadOrRepair(): CatalogSnapshot {
        seeder.seedIfEmpty()
        return try {
            snapshotReader.read()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (corrupt: CorruptCatalogException) {
            AppLog.w("catalog.repair", corrupt.message ?: "corrupt catalog")
            writer.clearAll()
            seeder.seedIfEmpty()
            snapshotReader.read()
        }
    }
}
