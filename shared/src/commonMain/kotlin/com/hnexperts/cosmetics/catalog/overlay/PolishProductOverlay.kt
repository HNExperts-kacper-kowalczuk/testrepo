package com.hnexperts.cosmetics.catalog.overlay

import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.catalog.pipeline.CatalogBuilder
import com.hnexperts.cosmetics.catalog.pipeline.ObfProductDump
import com.hnexperts.cosmetics.catalog.pipeline.ObfProductRecord

/**
 * Curated GS1 Poland (590) barcodes that Open Beauty Facts often lacks.
 * Manufacturer INCI when the brand publishes it; otherwise a labelled
 * 400 ml transcript with [ObfProductRecord.verified] false — the pack wins.
 */
object PolishProductOverlay {
    const val SOURCE_MANUFACTURER: String = "pl-manufacturer"
    const val SOURCE_LABEL: String = "pl-label"

    val records: List<ObfProductRecord> = listOf(
        bambinoRodzinaMirabelka400(),
        ziajaAntyperspirantSensitiv(),
        ziajaAntyperspirantSoft()
    )

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

    private fun ziajaAntyperspirantSensitiv(): ObfProductRecord {
        return ObfProductRecord(
            id = "pl-ziaja-5901887019367",
            name = "Ziaja anty-perspirant w kremie Sensitiv 60 ml",
            brand = "Ziaja",
            category = "deodorant",
            inciRaw = ZIAJA_SENSITIV_INCI,
            usage = "LEAVE_ON",
            source = SOURCE_MANUFACTURER,
            verified = true,
            gtins = listOf("5901887019367")
        )
    }

    private fun ziajaAntyperspirantSoft(): ObfProductRecord {
        return ObfProductRecord(
            id = "pl-ziaja-5901887019374",
            name = "Ziaja anty-perspirant w kremie Soft 60 ml",
            brand = "Ziaja",
            category = "deodorant",
            inciRaw = ZIAJA_SOFT_INCI,
            usage = "LEAVE_ON",
            source = SOURCE_MANUFACTURER,
            verified = true,
            gtins = listOf("5901887019374")
        )
    }

    private const val BAMBINO_MIRABELKA_400_INCI: String =
        "Aqua, Sodium Myreth Sulfate, Coco-Glucoside, Cocamidopropyl Betaine, " +
            "Glycerin, Sodium Chloride, Sodium Cocoamphoacetate, Glycine Soja Oil, " +
            "Panthenol, Pantolactone, Glyceryl Oleate, Tocopherol, " +
            "Hydrogenated Palm Glycerides Citrate, Lactic Acid, Citric Acid, " +
            "Disodium EDTA, Sodium Benzoate, Parfum"

    private const val ZIAJA_SENSITIV_INCI: String =
        "Aqua (Water), Aluminum Chlorohydrate, Dimethicone, Steareth-2, " +
            "PPG-15 Stearyl Ether, Steareth-21, Parfum (Fragrance), Allantoin, " +
            "Citronellyl Methylcrotonate, Glyceryl Caprylate, Alpha-Isomethyl Ionone, " +
            "Coumarin, Linalool"

    private const val ZIAJA_SOFT_INCI: String =
        "Aqua (Water), Aluminum Chlorohydrate, Steareth-2, PPG-15 Stearyl Ether, " +
            "Dimethicone, Steareth-21, Parfum (Fragrance), Citronellyl Methylcrotonate, " +
            "Glyceryl Caprylate, Citrus Aurantium Peel Oil, Limonene, Hexyl Cinnamal, " +
            "Linalyl Acetate, Alpha-Isomethyl Ionone, Acetyl Cedrene, Linalool, " +
            "Vanillin, Pinene, Coumarin"
}
