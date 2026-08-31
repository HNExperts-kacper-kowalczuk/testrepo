package com.hnexperts.cosmetics.catalog.overlay

import com.hnexperts.cosmetics.catalog.pipeline.ObfProductRecord

internal object PolishZiajaOverlay {
    val records: List<ObfProductRecord> = listOf(
        creamAntiperspirant(
            id = "pl-ziaja-5901887019367",
            name = "Ziaja anty-perspirant w kremie Sensitiv 60 ml",
            gtin = "5901887019367",
            inci = SENSITIV_INCI
        ),
        creamAntiperspirant(
            id = "pl-ziaja-5901887019374",
            name = "Ziaja anty-perspirant w kremie Soft 60 ml",
            gtin = "5901887019374",
            inci = SOFT_INCI
        ),
        rinseCleanser(
            id = "pl-ziaja-5901887049449",
            name = "Ziaja żel pod prysznic mięta herbaciana",
            gtin = "5901887049449",
            inci = MINT_SHOWER_INCI
        ),
        leaveOn(
            id = "pl-ziaja-5901887007210",
            name = "Ziaja Sport aqua tonik do ciała i twarzy 120 ml",
            category = "toner",
            usage = "SPRAY",
            gtin = "5901887007210",
            inci = SPORT_AQUA_INCI
        ),
        leaveOn(
            id = "pl-ziaja-5901887007234",
            name = "Ziaja Sport 2 w 1 balsam do ciała i odżywka do włosów 200 ml",
            category = "balm",
            usage = "LEAVE_ON",
            gtin = "5901887007234",
            inci = SPORT_BALM_INCI
        ),
        rinseCleanser(
            id = "pl-ziaja-5901887009924",
            name = "Ziaja kozie mleko kremowy żel myjący",
            gtin = "5901887009924",
            inci = GOAT_GEL_INCI
        ),
        rinseCleanser(
            id = "pl-ziaja-5901887032601",
            name = "Ziaja kozie mleko szampon z keratyną",
            gtin = "5901887032601",
            inci = GOAT_SHAMPOO_INCI
        )
    )

    private fun creamAntiperspirant(
        id: String,
        name: String,
        gtin: String,
        inci: String
    ): ObfProductRecord {
        return leaveOn(
            id = id,
            name = name,
            category = "deodorant",
            usage = "LEAVE_ON",
            gtin = gtin,
            inci = inci
        )
    }

    private fun rinseCleanser(
        id: String,
        name: String,
        gtin: String,
        inci: String
    ): ObfProductRecord {
        return ObfProductRecord(
            id = id,
            name = name,
            brand = BRAND,
            category = "cleanser",
            inciRaw = inci,
            usage = "RINSE_OFF",
            source = PolishProductOverlay.SOURCE_MANUFACTURER,
            verified = true,
            gtins = listOf(gtin)
        )
    }

    private fun leaveOn(
        id: String,
        name: String,
        category: String,
        usage: String,
        gtin: String,
        inci: String
    ): ObfProductRecord {
        return ObfProductRecord(
            id = id,
            name = name,
            brand = BRAND,
            category = category,
            inciRaw = inci,
            usage = usage,
            source = PolishProductOverlay.SOURCE_MANUFACTURER,
            verified = true,
            gtins = listOf(gtin)
        )
    }

    private const val BRAND: String = "Ziaja"

    private const val SENSITIV_INCI: String =
        "Aqua (Water), Aluminum Chlorohydrate, Dimethicone, Steareth-2, " +
            "PPG-15 Stearyl Ether, Steareth-21, Parfum (Fragrance), Allantoin, " +
            "Citronellyl Methylcrotonate, Glyceryl Caprylate, Alpha-Isomethyl Ionone, " +
            "Coumarin, Linalool"

    private const val SOFT_INCI: String =
        "Aqua (Water), Aluminum Chlorohydrate, Steareth-2, PPG-15 Stearyl Ether, " +
            "Dimethicone, Steareth-21, Parfum (Fragrance), Citronellyl Methylcrotonate, " +
            "Glyceryl Caprylate, Citrus Aurantium Peel Oil, Limonene, Hexyl Cinnamal, " +
            "Linalyl Acetate, Alpha-Isomethyl Ionone, Acetyl Cedrene, Linalool, " +
            "Vanillin, Pinene, Coumarin"

    private const val MINT_SHOWER_INCI: String =
        "Aqua (Water), Cocamidopropyl Betaine, Disodium Laureth Sulfosuccinate, " +
            "Decyl Glucoside, Inulin, Fructose, C12-13 Alkyl Lactate, " +
            "PEG-120 Methyl Glucose Dioleate, Sodium Chloride, Sodium Benzoate, " +
            "Parfum (Fragrance), Linalool, Geraniol, Carvone, Camphor, Linalyl Acetate, " +
            "Citric Acid"

    private const val SPORT_AQUA_INCI: String =
        "Aqua (Water), Glycerin, PPG-26-Buteth-26, PEG-40 Hydrogenated Castor Oil, " +
            "Sea Water, Hydrolyzed Lupine Protein, Sodium Benzoate, Parfum (Fragrance), " +
            "Citric Acid"

    private const val SPORT_BALM_INCI: String =
        "Aqua (Water), Isopropyl Myristate, Behenyl Alcohol, Propylene Glycol, " +
            "Distearoylethyl Hydroxyethylmonium Methosulfate, Cetearyl Alcohol, " +
            "Parfum (Fragrance), Cetyl Palmitate, Ceteareth-20, Glycerin, Dimethicone, " +
            "PCA Glyceryl Oleate, Sea Water, Lecithin, Ubiquinone, Hydrolyzed Lupine Protein, " +
            "Sodium Lactate, Cetyl Hydroxyethylcellulose, Pentylene Glycol, " +
            "Ethylhexylglycerin, Hydroxyacetophenone"

    private const val GOAT_GEL_INCI: String =
        "Aqua (Water), Sodium Laureth Sulfate, Cocamidopropyl Betaine, Glycerin, " +
            "Glycol Distearate, Glyceryl Oleate, PEG-7 Glyceryl Cocoate, Panthenol, " +
            "Lactobacillus/Milk Solids/Glycine Soja (Soybean) Oil Ferment, Cyclodextrin, " +
            "Propylene Glycol, Goat Milk Extract, C12-13 Alkyl Lactate, Sodium Chloride, " +
            "Sodium Benzoate, Parfum (Fragrance), Citric Acid"

    private const val GOAT_SHAMPOO_INCI: String =
        "Aqua (Water), Sodium Laureth Sulfate, Cocamidopropyl Betaine, " +
            "Ammonium Lauryl Sulfate, Glycol Distearate, Laureth-4, " +
            "Guar Hydroxypropyltrimonium Chloride, Hydrolyzed Keratin, Propylene Glycol, " +
            "Goat Milk Extract, Sodium Chloride, Sodium Benzoate, Parfum (Fragrance), " +
            "Linalool, Citronellol, Hexyl Cinnamal, Terpineol, Eugenol, " +
            "Alpha-Isomethyl Ionone, Citric Acid"
}
