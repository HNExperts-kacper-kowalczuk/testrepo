package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.pipeline.CosingIngredientRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CosingAssemblerTest {
    private fun metadata(rawJson: String) = Json.parseToJsonElement(rawJson).jsonObject

    @Test
    fun mapsInventoryEntryWithFunctionsToLow() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata(
                """
                {"inciName":["SODIUM LAURYL SULFATE"],
                 "casNo":["151-21-3"],
                 "innName":["sodium lauryl sulfate"],
                 "annexNo":[],
                 "functionName":["CLEANSING","FOAMING"]}
                """
            )
        )
        assertNotNull(record)
        assertEquals("sodium-lauryl-sulfate", record.id)
        assertEquals("LOW", record.dangerLevel)
        assertEquals(listOf("CLEANSING", "FOAMING"), record.functionTags)
        assertTrue(record.regulatoryTags.none { tag -> tag.startsWith("ALLERGEN_") })
    }

    @Test
    fun annexTwoBecomesProhibitedWithBilingualComments() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["HYDROQUINONE"],"annexNo":["II"],"functionName":[]}""")
        )
        assertNotNull(record)
        assertEquals("PROHIBITED", record.dangerLevel)
        assertEquals(listOf("ANNEX_II"), record.regulatoryTags)
        assertEquals(setOf("en", "pl"), record.comments.map { comment -> comment.locale }.toSet())
    }

    @Test
    fun annexThreeBecomesRestricted() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["THIOGLYCOLIC ACID"],"annexNo":["III"],"functionName":[]}""")
        )
        assertNotNull(record)
        assertEquals("RESTRICTED", record.dangerLevel)
    }

    @Test
    fun namesWithDigitCommaGetCommaException() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["1,2-HEXANEDIOL"],"annexNo":[],"functionName":["HUMECTANT"]}""")
        )
        assertNotNull(record)
        assertTrue(record.commaException)
        assertEquals("1-2-hexanediol", record.id)
    }

    @Test
    fun duplicateSlugsStayUnique() {
        val assembler = CosingAssembler()
        val first = assembler.toRecord(metadata("""{"inciName":["AQUA"],"functionName":["SOLVENT"]}"""))
        val second = assembler.toRecord(metadata("""{"inciName":["Aqua"],"functionName":["SOLVENT"]}"""))
        assertEquals("aqua", first?.id)
        assertEquals("aqua-2", second?.id)
    }

    @Test
    fun entryWithoutAnyNameIsSkipped() {
        val record = CosingAssembler().toRecord(metadata("""{"casNo":["123-45-6"]}"""))
        assertEquals(null, record)
    }

    @Test
    fun hexylCinnamalGetsAllergen26Tag() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["HEXYL CINNAMAL"],"annexNo":["III"],"functionName":["PERFUMING"]}""")
        )
        assertNotNull(record)
        assertEquals("hexyl-cinnamal", record.id)
        assertTrue(record.regulatoryTags.contains("ALLERGEN_26"))
        assertTrue(record.regulatoryTags.contains("ANNEX_III"))
    }

    @Test
    fun vanillinGetsAllergen80Tag() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["VANILLIN"],"annexNo":[],"functionName":["PERFUMING"]}""")
        )
        assertNotNull(record)
        assertEquals("LOW", record.dangerLevel)
        assertEquals(listOf("ALLERGEN_80"), record.regulatoryTags)
    }

    @Test
    fun polyethyleneGetsMicroplasticTagWithoutChangingDanger() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["POLYETHYLENE"],"annexNo":[],"functionName":["ABRASIVE"]}""")
        )
        assertNotNull(record)
        assertEquals("LOW", record.dangerLevel)
        assertTrue(record.regulatoryTags.contains("MICROPLASTIC"))
        assertEquals(listOf("ABRASIVE"), record.functionTags)
    }

    @Test
    fun carmineGetsAnimalDerivedTagWithoutChangingDanger() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["CARMINE"],"annexNo":[],"functionName":["COLORANT"]}""")
        )
        assertNotNull(record)
        assertEquals("LOW", record.dangerLevel)
        assertTrue(record.regulatoryTags.contains("ANIMAL_DERIVED"))
        assertEquals(listOf("COLORANT"), record.functionTags)
    }

    @Test
    fun salicylicAcidGetsSunChildrenAndPregnancyTags() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["SALICYLIC ACID"],"annexNo":["III"],"functionName":["KERATOLYTIC"]}""")
        )
        assertNotNull(record)
        assertEquals("RESTRICTED", record.dangerLevel)
        assertTrue(record.regulatoryTags.contains("ANNEX_III"))
        assertTrue(record.regulatoryTags.contains("PHOTOTOXIC"))
        assertTrue(record.regulatoryTags.contains("CHILDREN"))
        assertTrue(record.regulatoryTags.contains("PREGNANCY_CAUTION"))
    }

    @Test
    fun retinalGetsPregnancyTagWithoutChangingLow() {
        val record: CosingIngredientRecord? = CosingAssembler().toRecord(
            metadata("""{"inciName":["RETINAL"],"annexNo":[],"functionName":["SKIN CONDITIONING"]}""")
        )
        assertNotNull(record)
        assertEquals("LOW", record.dangerLevel)
        assertTrue(record.regulatoryTags.contains("PREGNANCY_CAUTION"))
    }
}
