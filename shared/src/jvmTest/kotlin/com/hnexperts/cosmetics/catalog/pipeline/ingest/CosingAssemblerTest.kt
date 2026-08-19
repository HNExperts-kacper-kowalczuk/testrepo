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
}
