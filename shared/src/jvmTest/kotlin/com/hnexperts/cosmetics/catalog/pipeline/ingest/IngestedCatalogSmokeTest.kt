package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.pipeline.CatalogSourceCodec
import com.hnexperts.cosmetics.catalog.pipeline.CosingIngredientDump
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.IngredientMatcher
import com.hnexperts.cosmetics.ingredients.domain.IngredientRef
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs only when a generated ingest dump is present (after
 * `./gradlew :shared:ingestCatalogSources`). Guards that the CosIng-scale
 * dataset parses through the pipeline codec and that the matcher resolves a
 * typical label against 36k ingredients.
 */
class IngestedCatalogSmokeTest {
    @Test
    fun ingestedDumpParsesAndMatchesATypicalLabel() {
        val path: Path = Path.of("../catalog/ingest/cosing-ingredients.json")
        if (!Files.exists(path)) {
            println("SKIP: no ingest dump at $path")
            return
        }
        val dump: CosingIngredientDump = CatalogSourceCodec.parseIngredients(Files.readString(path))
        assertTrue(dump.ingredients.size > 30000, "expected CosIng-scale dump")

        val ingredients: List<Ingredient> = dump.ingredients.map { record ->
            Ingredient(
                id = record.id,
                inciName = record.inciName,
                casNumbers = record.casNumbers,
                functionTags = record.functionTags
            )
        }
        val aliases: Map<String, String> = dump.ingredients.flatMap { record ->
            record.aliases.map { alias -> alias to record.id }
        }.toMap()
        val commaExceptions: List<String> = dump.ingredients
            .filter { record -> record.commaException }
            .map { record -> record.inciName }
        val matcher = IngredientMatcher(ingredients, aliases, commaExceptions)

        val refs: List<IngredientRef> = matcher.matchList(
            "Aqua, Caprylic/Capric Triglyceride, Glycerin, Alcohol Denat., Parfum, Limonene"
        )
        assertEquals(6, refs.size)
        val unmatched: List<String> = refs
            .filter { ref -> ref.matchedBy == MatchMethod.UNMATCHED }
            .map { ref -> ref.displayName }
        assertEquals(emptyList(), unmatched)
    }
}
