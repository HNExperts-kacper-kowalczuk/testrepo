package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.ingredients.domain.InciNormalizer

/**
 * Labelled fragrance allergens from Annex III of Regulation (EC) 1223/2009
 * as expanded by Regulation (EU) 2023/1545. Used at CosIng ingest time so
 * the EU-allergens preset stays data-driven.
 */
object EuLabelledAllergenIndex {
    fun tagsFor(inciName: String): List<String> {
        val key: String = InciNormalizer.normalize(inciName)
        if (key.isEmpty()) {
            return emptyList()
        }
        return buildList {
            if (ORIGINAL_26.contains(key)) {
                add("ALLERGEN_26")
            }
            if (EXPANDED_80.contains(key)) {
                add("ALLERGEN_80")
            }
        }
    }

    private val ORIGINAL_26: Set<String> = normalizeAll(
        "Amyl Cinnamal",
        "Benzyl Alcohol",
        "Cinnamyl Alcohol",
        "Citral",
        "Eugenol",
        "Hydroxycitronellal",
        "Isoeugenol",
        "Amylcinnamyl Alcohol",
        "Benzyl Salicylate",
        "Cinnamal",
        "Coumarin",
        "Geraniol",
        "Hydroxyisohexyl 3-Cyclohexene Carboxaldehyde",
        "Anise Alcohol",
        "Benzyl Cinnamate",
        "Farnesol",
        "Butylphenyl Methylpropional",
        "Linalool",
        "Benzyl Benzoate",
        "Citronellol",
        "Hexyl Cinnamal",
        "Limonene",
        "Methyl 2-Octynoate",
        "Alpha-Isomethyl Ionone",
        "Evernia Prunastri Extract",
        "Evernia Furfuracea Extract"
    )

    private val EXPANDED_80: Set<String> = normalizeAll(
        "Acetyl Cedrene",
        "Amyl Salicylate",
        "Anethole",
        "Benzaldehyde",
        "Camphor",
        "Beta-Caryophyllene",
        "Carvone",
        "Dimethyl Phenethyl Acetate",
        "Hexadecanolactone",
        "Hexamethylindanopyran",
        "Linalyl Acetate",
        "Menthol",
        "Methyl Salicylate",
        "Pinene",
        "Alpha-Pinene",
        "Beta-Pinene",
        "Santalol",
        "Sclareol",
        "Terpineol",
        "Tetramethyl Acetyloctahydronaphthalenes",
        "Trimethylbenzenepropanol",
        "Trimethylcyclopentenyl Methylisopentenol",
        "Vanillin",
        "Eugenyl Acetate",
        "Geranyl Acetate",
        "Isoeugenyl Acetate",
        "Cananga Odorata Oil/Extract",
        "Cinnamomum Cassia Leaf Oil",
        "Cinnamomum Zeylanicum Bark Oil",
        "Citrus Aurantium Flower Oil",
        "Citrus Aurantium Peel Oil",
        "Citrus Aurantium Bergamia Peel Oil",
        "Citrus Limon Peel Oil",
        "Eucalyptus Globulus Oil",
        "Eugenia Caryophyllus Oil",
        "Juniperus Virginiana Oil",
        "Laurus Nobilis Leaf Oil",
        "Lavandula Oil",
        "Mentha Piperita Oil",
        "Mentha Viridis Leaf Oil",
        "Pelargonium Graveolens Flower Oil",
        "Pogostemon Cablin Oil",
        "Santalum Album Oil",
        "Myroxylon Pereirae Oil/Extract",
        "Pinus Mugo",
        "Pinus Pumila",
        "Cedrus Atlantica Oil",
        "Rose Flower Oil"
    )

    private fun normalizeAll(vararg names: String): Set<String> {
        return names.map { name -> InciNormalizer.normalize(name) }.toSet()
    }
}
