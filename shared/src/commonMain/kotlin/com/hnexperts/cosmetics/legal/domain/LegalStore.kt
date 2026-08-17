package com.hnexperts.cosmetics.legal.domain

import com.hnexperts.cosmetics.failure.Outcome

data class LegalState(
    val disclaimerAccepted: Boolean
)

interface LegalStore {
    suspend fun load(): Outcome<LegalState>
    suspend fun acceptDisclaimer(): Outcome<Unit>
}
