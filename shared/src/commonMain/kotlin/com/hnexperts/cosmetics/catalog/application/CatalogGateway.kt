package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.failure.Outcome

interface CatalogGateway {
    suspend fun awaitIndex(): Outcome<CatalogIndex>
}
