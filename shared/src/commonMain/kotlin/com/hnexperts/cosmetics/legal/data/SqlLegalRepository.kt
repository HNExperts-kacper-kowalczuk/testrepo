package com.hnexperts.cosmetics.legal.data

import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.data.userdb.UserDatabase
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.legal.domain.LegalState
import com.hnexperts.cosmetics.legal.domain.LegalStore
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class SqlLegalRepository(
    private val database: UserDatabase,
    private val dispatchers: AppDispatchers
) : LegalStore {
    override suspend fun load(): Outcome<LegalState> {
        return FailureCatcher.database("legal.load") {
            withContext(dispatchers.userDatabase) {
                val row = database.userDatabaseQueries.selectLegal().executeAsOneOrNull()
                LegalState(disclaimerAccepted = row?.disclaimer_accepted == 1L)
            }
        }
    }

    override suspend fun acceptDisclaimer(): Outcome<Unit> {
        return FailureCatcher.database("legal.accept") {
            withContext(dispatchers.userDatabase) {
                database.userDatabaseQueries.upsertLegal(
                    disclaimer_accepted = 1,
                    accepted_at = Clock.System.now().toString()
                )
            }
        }
    }
}
