package com.hnexperts.cosmetics.catalog.fixture

import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.ingredients.domain.Ingredient

data class FixtureIngredient(
    val ingredient: Ingredient,
    val aliases: List<String>,
    val commaException: Boolean,
    val hazard: IngredientHazard,
    val comments: List<LocalizedText>
)

data class FixtureProduct(
    val product: Product,
    val gtins: List<String>
)

object FixtureCatalog {
    const val RULESET_VERSION: String = "2026.08-fixture"
    const val CATALOG_VERSION: String = "2026.08-fixture"

    val ingredients: List<FixtureIngredient> = listOf(
        ing(
            id = "aqua",
            name = "Aqua",
            aliases = listOf("Water", "Eau"),
            level = DangerLevel.SAFE,
            tags = listOf("SOLVENT"),
            en = "Water. No relevant restriction in this ruleset.",
            pl = "Woda. Brak istotnych ograniczeń w tym zestawie reguł."
        ),
        ing(
            id = "glycerin",
            name = "Glycerin",
            aliases = listOf("Glycerol"),
            level = DangerLevel.SAFE,
            tags = listOf("HUMECTANT"),
            en = "Common humectant, generally accepted in cosmetics.",
            pl = "Popularny humektant, zwykle akceptowany w kosmetykach."
        ),
        ing(
            id = "niacinamide",
            name = "Niacinamide",
            aliases = emptyList(),
            level = DangerLevel.SAFE,
            tags = listOf("SKIN_CONDITIONING"),
            en = "Vitamin B3 derivative used for skin conditioning.",
            pl = "Pochodna witaminy B3 stosowana do kondycjonowania skóry."
        ),
        ing(
            id = "tocopherol",
            name = "Tocopherol",
            aliases = listOf("Vitamin E"),
            level = DangerLevel.SAFE,
            tags = listOf("ANTIOXIDANT"),
            en = "Vitamin E antioxidant. Generally accepted at typical use.",
            pl = "Przeciwutleniacz — witamina E. Zwykle akceptowany."
        ),
        ing(
            id = "zinc-oxide",
            name = "Zinc Oxide",
            aliases = listOf("CI 77947"),
            level = DangerLevel.SAFE,
            tags = listOf("UV_FILTER"),
            en = "Mineral UV filter authorised in the EU with concentration limits.",
            pl = "Mineralny filtr UV dopuszczony w UE z limitami stężenia."
        ),
        ing(
            id = "dimethicone",
            name = "Dimethicone",
            aliases = emptyList(),
            level = DangerLevel.SAFE,
            tags = listOf("EMOLLIENT"),
            en = "Silicone emollient. No relevant restriction in this ruleset.",
            pl = "Emolient silikonowy. Brak istotnych ograniczeń w tym zestawie reguł."
        ),
        ing(
            id = "petrolatum",
            name = "Petrolatum",
            aliases = listOf("Vaseline", "Petroleum Jelly"),
            level = DangerLevel.SAFE,
            tags = listOf("EMOLLIENT"),
            en = "Occlusive emollient. Cosmetic grades are highly refined.",
            pl = "Emolient okluzyjny. Gatunki kosmetyczne są wysoko oczyszczone."
        ),
        ing(
            id = "cetyl-alcohol",
            name = "Cetyl Alcohol",
            aliases = emptyList(),
            level = DangerLevel.SAFE,
            tags = listOf("EMOLLIENT", "EMULSIFIER"),
            en = "Fatty alcohol used as an emollient and thickener.",
            pl = "Alkohol tłuszczowy stosowany jako emolient i zagęstnik."
        ),
        ing(
            id = "cocamidopropyl-betaine",
            name = "Cocamidopropyl Betaine",
            aliases = listOf("CAPB"),
            level = DangerLevel.LOW,
            tags = listOf("SURFACTANT"),
            en = "Mild surfactant. Can occasionally sensitise, especially if impure.",
            pl = "Łagodny środek powierzchniowo czynny. Czasem może uczulać."
        ),
        ing(
            id = "phenoxyethanol",
            name = "Phenoxyethanol",
            aliases = emptyList(),
            level = DangerLevel.LOW,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "EU-authorised preservative with a maximum concentration.",
            pl = "Konserwant dopuszczony w UE z maksymalnym stężeniem."
        ),
        ing(
            id = "sodium-benzoate",
            name = "Sodium Benzoate",
            aliases = emptyList(),
            level = DangerLevel.LOW,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Authorised preservative; more common in rinse-off products.",
            pl = "Dopuszczony konserwant; częstszy w produktach spłukiwanych."
        ),
        ing(
            id = "alcohol-denat",
            name = "Alcohol Denat.",
            aliases = listOf("Alcohol Denatured", "Ethanol"),
            level = DangerLevel.LOW,
            tags = listOf("SOLVENT"),
            en = "Denatured alcohol. Can dry or irritate in leave-on products.",
            pl = "Denaturowany alkohol. W produktach leave-on może wysuszać lub podrażniać."
        ),
        ing(
            id = "hexanediol",
            name = "1,2-Hexanediol",
            aliases = emptyList(),
            commaException = true,
            level = DangerLevel.LOW,
            tags = listOf("HUMECTANT", "PRESERVATIVE"),
            en = "Humectant and mild preservative booster. Name contains a comma.",
            pl = "Humektant i wspomaganie konserwacji. Nazwa zawiera przecinek."
        ),
        ing(
            id = "titanium-dioxide",
            name = "Titanium Dioxide",
            aliases = listOf("CI 77891", "CI77891"),
            level = DangerLevel.LOW,
            tags = listOf("UV_FILTER", "COLORANT", "ANNEX_VI"),
            en = "Mineral pigment and UV filter. Nano forms must be labelled.",
            pl = "Mineralny pigment i filtr UV. Formy nano muszą być oznaczone."
        ),
        ing(
            id = "ci-19140",
            name = "CI 19140",
            aliases = listOf("Yellow 5", "Tartrazine"),
            level = DangerLevel.LOW,
            tags = listOf("COLORANT", "ANNEX_IV"),
            en = "Yellow colourant authorised in cosmetics under Annex IV.",
            pl = "Żółty barwnik dopuszczony w kosmetykach (załącznik IV)."
        ),
        ing(
            id = "sodium-lauryl-sulfate",
            name = "Sodium Lauryl Sulfate",
            aliases = listOf("SLS", "Sodium Lauryl Sulphate"),
            level = DangerLevel.MODERATE,
            tags = listOf("SURFACTANT"),
            en = "Strong surfactant. Can irritate skin and eyes, especially in leave-on use.",
            pl = "Silny środek powierzchniowo czynny. Może podrażniać skórę i oczy."
        ),
        ing(
            id = "sodium-laureth-sulfate",
            name = "Sodium Laureth Sulfate",
            aliases = listOf("SLES", "Sodium Laureth Sulphate"),
            level = DangerLevel.MODERATE,
            tags = listOf("SURFACTANT"),
            en = "Common cleanser. Milder than SLS but still a frequent irritant.",
            pl = "Popularny środek myjący. Łagodniejszy niż SLS, ale nadal bywa drażniący."
        ),
        ing(
            id = "parfum",
            name = "Parfum",
            aliases = listOf("Fragrance", "Aroma"),
            level = DangerLevel.MODERATE,
            tags = listOf("FRAGRANCE"),
            en = "Undisclosed fragrance mix. May include EU-labelled allergens.",
            pl = "Niejawna mieszanina zapachowa. Może zawierać alergeny oznaczane w UE."
        ),
        ing(
            id = "limonene",
            name = "Limonene",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("FRAGRANCE", "ALLERGEN_26"),
            en = "Common fragrance allergen that must be labelled above a threshold.",
            pl = "Częsty alergen zapachowy, który powyżej progu musi być wymieniony."
        ),
        ing(
            id = "linalool",
            name = "Linalool",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("FRAGRANCE", "ALLERGEN_26"),
            en = "Common fragrance allergen that must be labelled above a threshold.",
            pl = "Częsty alergen zapachowy, który powyżej progu musi być wymieniony."
        ),
        ing(
            id = "citral",
            name = "Citral",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("FRAGRANCE", "ALLERGEN_26"),
            en = "Fragrance allergen listed in the EU allergen annex.",
            pl = "Alergen zapachowy wymieniony w unijnym wykazie alergenów."
        ),
        ing(
            id = "hydroxycitronellal",
            name = "Hydroxycitronellal",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("FRAGRANCE", "ALLERGEN_26"),
            en = "Fragrance allergen listed in the EU allergen annex.",
            pl = "Alergen zapachowy wymieniony w unijnym wykazie alergenów."
        ),
        ing(
            id = "homosalate",
            name = "Homosalate",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("UV_FILTER", "ANNEX_VI"),
            en = "Chemical UV filter with concentration limits and ongoing scrutiny.",
            pl = "Chemiczny filtr UV z limitami stężenia i trwającą oceną bezpieczeństwa."
        ),
        ing(
            id = "propylparaben",
            name = "Propylparaben",
            aliases = emptyList(),
            level = DangerLevel.MODERATE,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Paraben preservative allowed only under concentration limits.",
            pl = "Paraben dopuszczony wyłącznie w określonych stężeniach."
        ),
        ing(
            id = "triclosan",
            name = "Triclosan",
            aliases = emptyList(),
            level = DangerLevel.RESTRICTED,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Restricted preservative. Allowed only in specific product types and concentrations.",
            pl = "Ograniczony konserwant. Dopuszczony tylko w określonych produktach i stężeniach."
        ),
        ing(
            id = "benzophenone-3",
            name = "Benzophenone-3",
            aliases = listOf("Oxybenzone"),
            level = DangerLevel.RESTRICTED,
            tags = listOf("UV_FILTER", "ANNEX_VI"),
            en = "UV filter restricted in the EU; concentration and labelling conditions apply.",
            pl = "Filtr UV ograniczony w UE; obowiązują limity i oznakowanie."
        ),
        ing(
            id = "salicylic-acid",
            name = "Salicylic Acid",
            aliases = listOf("BHA Acid"),
            level = DangerLevel.RESTRICTED,
            tags = listOf("KERATOLYTIC", "ANNEX_III", "PREGNANCY_CAUTION"),
            en = "Restricted keratolytic. Not for children under three; caution in pregnancy.",
            pl = "Ograniczony keratolotyk. Nie dla dzieci poniżej 3 lat; ostrożnie w ciąży."
        ),
        ing(
            id = "retinol",
            name = "Retinol",
            aliases = listOf("Vitamin A"),
            level = DangerLevel.RESTRICTED,
            tags = listOf("ANNEX_III", "PREGNANCY_CAUTION"),
            en = "Vitamin A. Concentration-capped in the EU; often avoided in pregnancy.",
            pl = "Witamina A. W UE z limitem stężenia; często unikana w ciąży."
        ),
        ing(
            id = "methylisothiazolinone",
            name = "Methylisothiazolinone",
            aliases = listOf("MIT"),
            level = DangerLevel.HIGH,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Strong sensitiser. Banned in leave-on cosmetics in the EU.",
            pl = "Silny alergen. W UE zakazany w kosmetykach leave-on."
        ),
        ing(
            id = "methylchloroisothiazolinone",
            name = "Methylchloroisothiazolinone",
            aliases = listOf("MCI"),
            level = DangerLevel.HIGH,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Used with MIT in a restricted rinse-off mix. High allergy concern.",
            pl = "Stosowany z MIT w ograniczonej mieszaninie do spłukiwania. Wysokie ryzyko alergii."
        ),
        ing(
            id = "butylparaben",
            name = "Butylparaben",
            aliases = emptyList(),
            level = DangerLevel.HIGH,
            tags = listOf("PRESERVATIVE", "ANNEX_V"),
            en = "Paraben with tighter restrictions than some other parabens.",
            pl = "Paraben z ostrzejszymi ograniczeniami niż niektóre inne parabeny."
        ),
        ing(
            id = "bha",
            name = "BHA",
            aliases = listOf("Butylated Hydroxyanisole"),
            level = DangerLevel.HIGH,
            tags = listOf("ANTIOXIDANT"),
            en = "Antioxidant with significant safety concern in leave-on cosmetics.",
            pl = "Przeciwutleniacz budzący istotne zastrzeżenia w kosmetykach leave-on."
        ),
        ing(
            id = "cyclotetrasiloxane",
            name = "Cyclotetrasiloxane",
            aliases = listOf("D4"),
            level = DangerLevel.HIGH,
            tags = listOf("EMOLLIENT"),
            en = "Cyclic silicone (D4) with serious environmental and health restrictions.",
            pl = "Cykliczny silikon (D4) z poważnymi ograniczeniami zdrowotnymi i środowiskowymi."
        ),
        ing(
            id = "formaldehyde",
            name = "Formaldehyde",
            aliases = listOf("Methanal"),
            level = DangerLevel.PROHIBITED,
            tags = listOf("ANNEX_II", "CMR"),
            en = "Listed as prohibited in EU cosmetics (Annex II).",
            pl = "Wykazany jako substancja zakazana w kosmetykach UE (załącznik II)."
        )
    )

    val products: List<FixtureProduct> = listOf(
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

    fun commaExceptions(): List<String> {
        return ingredients.filter { item -> item.commaException }.map { item -> item.ingredient.inciName }
    }

    fun aliasMap(): Map<String, String> {
        val result: MutableMap<String, String> = mutableMapOf()
        for (item in ingredients) {
            for (alias in item.aliases) {
                result[alias] = item.ingredient.id
            }
        }
        return result
    }

    private fun ing(
        id: String,
        name: String,
        aliases: List<String>,
        level: DangerLevel,
        tags: List<String>,
        en: String,
        pl: String,
        commaException: Boolean = false
    ): FixtureIngredient {
        val functionTags: List<String> = tags.filter { tag ->
            tag != "ANNEX_II" && tag != "ANNEX_III" && tag != "ANNEX_IV" &&
                tag != "ANNEX_V" && tag != "ANNEX_VI" && tag != "ALLERGEN_26" &&
                tag != "CMR" && tag != "PREGNANCY_CAUTION"
        }
        val regulatoryTags: List<String> = tags.filter { tag ->
            tag.startsWith("ANNEX_") || tag == "ALLERGEN_26" || tag == "CMR" || tag == "PREGNANCY_CAUTION"
        }
        return FixtureIngredient(
            ingredient = Ingredient(
                id = id,
                inciName = name,
                casNumbers = null,
                functionTags = functionTags
            ),
            aliases = aliases,
            commaException = commaException,
            hazard = IngredientHazard(
                ingredientId = id,
                dangerLevel = level,
                regulatoryTags = regulatoryTags,
                restrictionJson = null
            ),
            comments = listOf(
                LocalizedText(locale = "en", summary = en, detail = null),
                LocalizedText(locale = "pl", summary = pl, detail = null)
            )
        )
    }
}
