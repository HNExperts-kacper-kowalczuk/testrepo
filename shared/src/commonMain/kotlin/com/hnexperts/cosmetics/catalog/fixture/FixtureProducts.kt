package com.hnexperts.cosmetics.catalog.fixture

import com.hnexperts.cosmetics.catalog.domain.Product

internal object FixtureProducts {
    val all: List<FixtureProduct> = listOf(
        FixtureProduct(
            product = Product(
                id = "gentle-cleanser",
                name = "Gentle Cream Cleanser",
                brand = "Fixture Care",
                category = "cleanser",
                inciRaw = "Aqua, Glycerin, Cocamidopropyl Betaine, Phenoxyethanol, Sodium Benzoate, 1,2-Hexanediol",
                usage = "RINSE_OFF",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123457")
        ),
        FixtureProduct(
            product = Product(
                id = "strong-shampoo",
                name = "Deep Clean Shampoo",
                brand = "Fixture Care",
                category = "shampoo",
                inciRaw = "Aqua, Sodium Lauryl Sulfate, Sodium Laureth Sulfate, Cocamidopropyl Betaine, Parfum, Limonene, Linalool, Methylisothiazolinone, Methylchloroisothiazolinone",
                usage = "RINSE_OFF",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123464")
        ),
        FixtureProduct(
            product = Product(
                id = "daily-sunscreen",
                name = "Daily Mineral Sunscreen",
                brand = "Fixture Sun",
                category = "sunscreen",
                inciRaw = "Aqua, Zinc Oxide, Titanium Dioxide, Homosalate, Dimethicone, Glycerin, Tocopherol, CI 19140",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123471")
        ),
        FixtureProduct(
            product = Product(
                id = "night-cream",
                name = "Renew Night Cream",
                brand = "Fixture Lab",
                category = "moisturizer",
                inciRaw = "Aqua, Petrolatum, Cetyl Alcohol, Glycerin, Retinol, Salicylic Acid, Phenoxyethanol, Parfum, Limonene",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123488")
        ),
        FixtureProduct(
            product = Product(
                id = "fragrance-mist",
                name = "Citrus Body Mist",
                brand = "Fixture Bloom",
                category = "fragrance",
                inciRaw = "Aqua, Alcohol Denat., Parfum, Limonene, Linalool, Citral, Hydroxycitronellal, CI 19140",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123495")
        ),
        FixtureProduct(
            product = Product(
                id = "problem-paste",
                name = "Banned Actives Demo Paste",
                brand = "Fixture Demo",
                category = "treatment",
                inciRaw = "Aqua, Formaldehyde, Triclosan, Butylparaben, BHA, Cyclotetrasiloxane, Benzophenone-3",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123501")
        ),
        FixtureProduct(
            product = Product(
                id = "niacinamide-serum",
                name = "Niacinamide Serum 10%",
                brand = "Fixture Lab",
                category = "serum",
                inciRaw = "Aqua, Niacinamide, Glycerin, 1,2-Hexanediol, Phenoxyethanol, Tocopherol",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123518", "05901234123518")
        ),
        FixtureProduct(
            product = Product(
                id = "simple-balm",
                name = "Plain Petrolatum Balm",
                brand = "Fixture Care",
                category = "balm",
                inciRaw = "Petrolatum, Tocopherol",
                usage = "LEAVE_ON",
                source = "curated",
                verified = true
            ),
            gtins = listOf("5901234123525")
        )
    )
}
