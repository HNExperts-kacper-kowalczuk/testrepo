package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.logging.AppLog

class LocalPublishedCatalogRemote(
    private val catalog: CatalogGateway
) : CatalogRemote {
    override suspend fun publishedManifest(): CatalogManifest {
        return when (val ready: Outcome<CatalogIndex> = catalog.awaitIndex()) {
            is Outcome.Err -> {
                AppLog.w("catalog.remote", ready.failure.verboseMessage())
                CatalogIntegrity.fixtureMeta().toManifest(
                    productCount = FixtureCatalog.products.size,
                    ingredientCount = FixtureCatalog.ingredients.size
                )
            }
            is Outcome.Ok -> ready.value.meta.toManifest(
                productCount = 0,
                ingredientCount = ready.value.ingredientsSorted.size
            )
        }
    }
}
