package com.hnexperts.cosmetics.catalog.overlay

import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.catalog.pipeline.CatalogBuilder
import com.hnexperts.cosmetics.catalog.pipeline.ObfProductDump
import com.hnexperts.cosmetics.catalog.pipeline.ObfProductRecord

/**
 * Curated GS1 Poland (590) barcodes that Open Beauty Facts often lacks.
 * Manufacturer INCI when the brand publishes it; otherwise a labelled
 * transcript with [ObfProductRecord.verified] false — the pack wins.
 */
object PolishProductOverlay {
    const val SOURCE_MANUFACTURER: String = "pl-manufacturer"
    const val SOURCE_LABEL: String = "pl-label"

    val records: List<ObfProductRecord> = listOf(bambinoRodzinaMirabelka400()) + PolishZiajaOverlay.records

    val dump: ObfProductDump = ObfProductDump(
        source = "pl-curated",
        region = "PL",
        products = records
    )

    val products: List<FixtureProduct>
        get() = records.map(CatalogBuilder::productFrom)

    private fun bambinoRodzinaMirabelka400(): ObfProductRecord {
        return ObfProductRecord(
            id = "pl-bambino-5900017071398",
            name = "Bambino Rodzina żel pod prysznic Mirabelka 400 ml",
            brand = "Bambino",
            category = "cleanser",
            inciRaw = BAMBINO_MIRABELKA_400_INCI,
            usage = "RINSE_OFF",
            source = SOURCE_LABEL,
            verified = false,
            gtins = listOf("5900017071398")
        )
    }

    private const val BAMBINO_MIRABELKA_400_INCI: String =
        "Aqua, Sodium Myreth Sulfate, Coco-Glucoside, Cocamidopropyl Betaine, " +
            "Glycerin, Sodium Chloride, Sodium Cocoamphoacetate, Glycine Soja Oil, " +
            "Panthenol, Pantolactone, Glyceryl Oleate, Tocopherol, " +
            "Hydrogenated Palm Glycerides Citrate, Lactic Acid, Citric Acid, " +
            "Disodium EDTA, Sodium Benzoate, Parfum"
}
