package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.failure.FailureCatcher
import com.hnexperts.cosmetics.failure.Outcome

class BundledCatalogRemote : CatalogRemote {
    override suspend fun publishedManifest(): CatalogManifest {
        val meta: CatalogMeta = CatalogIntegrity.fixtureMeta()
        return CatalogManifest(
            catalogVersion = meta.catalogVersion,
            rulesetVersion = meta.rulesetVersion,
            builtAt = meta.builtAt,
            region = meta.region,
            checksum = meta.checksum,
            productCount = FixtureCatalog.products.size,
            ingredientCount = FixtureCatalog.ingredients.size
        )
    }
}

class CheckCatalogUpdates(
    private val catalog: CatalogGateway,
    private val remote: CatalogRemote,
    private val network: NetworkMonitor
) {
    suspend fun invoke(): Outcome<CatalogFreshness> {
        if (!network.isOnline()) {
            return Outcome.Ok(CatalogFreshness.Offline)
        }
        val ready: Outcome<CatalogIndex> = catalog.awaitIndex()
        val index: CatalogIndex = when (ready) {
            is Outcome.Err -> return ready
            is Outcome.Ok -> ready.value
        }
        return FailureCatcher.catalog("catalog.sync.check") {
            val published: CatalogManifest = remote.publishedManifest()
            val local: CatalogManifest = index.meta.toManifest(
                productCount = published.productCount,
                ingredientCount = index.ingredientsSorted.size
            )
            if (local.catalogVersion == published.catalogVersion && local.checksum == published.checksum) {
                CatalogFreshness.UpToDate(local)
            } else {
                CatalogFreshness.UpdateAvailable(local = local, published = published)
            }
        }
    }
}

fun CatalogMeta.toManifest(productCount: Int, ingredientCount: Int): CatalogManifest {
    return CatalogManifest(
        catalogVersion = catalogVersion,
        rulesetVersion = rulesetVersion,
        builtAt = builtAt,
        region = region,
        checksum = checksum,
        productCount = productCount,
        ingredientCount = ingredientCount
    )
}
