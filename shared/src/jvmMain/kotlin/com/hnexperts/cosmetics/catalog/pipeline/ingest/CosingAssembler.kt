package com.hnexperts.cosmetics.catalog.pipeline.ingest

import com.hnexperts.cosmetics.catalog.pipeline.CosingCommentRecord
import com.hnexperts.cosmetics.catalog.pipeline.CosingIngredientRecord
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import kotlinx.serialization.json.JsonObject

/**
 * Maps CosIng inventory metadata to catalog ingredient records.
 *
 * Danger levels are derived only from the regulatory annexes: Annex II is
 * prohibited, Annex III restricted, IV/V/VI are authorised lists. Everything
 * else stays LOW (has recognised cosmetic functions) or UNKNOWN. Per-substance
 * expert comments are a separate curation step; only the templated EN/PL
 * comments required by the validator for PROHIBITED entries are generated.
 */
class CosingAssembler {
    private val usedIds: MutableMap<String, Int> = mutableMapOf()

    fun assemble(metadata: List<JsonObject>): List<CosingIngredientRecord> {
        return metadata.mapNotNull(::toRecord)
    }

    fun toRecord(metadata: JsonObject): CosingIngredientRecord? {
        val name: String = metadata.firstString("inciName")
            ?: metadata.firstString("nameOfCommonIngredientsGlossary")
            ?: return null
        val annexes: List<String> = metadata.allStrings("annexNo")
        val functions: List<String> = metadata.allStrings("functionName")
        val level: DangerLevel = dangerLevel(annexes, functions)
        return CosingIngredientRecord(
            id = uniqueId(slug(name)),
            inciName = name,
            casNumbers = metadata.firstString("casNo")?.takeIf { cas -> cas != "-" },
            aliases = aliases(metadata, name),
            commaException = COMMA_IN_NAME.containsMatchIn(name),
            dangerLevel = level.name,
            regulatoryTags = annexes.map { annex -> "ANNEX_$annex" },
            functionTags = functions.map { function -> function.uppercase().replace(' ', '_') },
            comments = templatedComments(level)
        )
    }

    private fun dangerLevel(annexes: List<String>, functions: List<String>): DangerLevel {
        return when {
            annexes.any { annex -> annex.startsWith("II") && !annex.startsWith("III") } -> DangerLevel.PROHIBITED
            annexes.any { annex -> annex.startsWith("III") } -> DangerLevel.RESTRICTED
            annexes.isNotEmpty() -> DangerLevel.LOW
            functions.isNotEmpty() -> DangerLevel.LOW
            else -> DangerLevel.UNKNOWN
        }
    }

    private fun aliases(metadata: JsonObject, name: String): List<String> {
        val candidates: List<String> = metadata.allStrings("innName") +
            metadata.allStrings("nameOfCommonIngredientsGlossary")
        return candidates
            .map { alias -> alias.uppercase() }
            .filter { alias -> alias != name.uppercase() }
            .distinct()
    }

    private fun templatedComments(level: DangerLevel): List<CosingCommentRecord> {
        return when (level) {
            DangerLevel.PROHIBITED -> listOf(
                CosingCommentRecord(
                    locale = "en",
                    summary = "Prohibited in EU cosmetics (Annex II of Regulation (EC) No 1223/2009)."
                ),
                CosingCommentRecord(
                    locale = "pl",
                    summary = "Substancja zakazana w kosmetykach w UE (załącznik II rozporządzenia (WE) nr 1223/2009)."
                )
            )
            DangerLevel.RESTRICTED -> listOf(
                CosingCommentRecord(
                    locale = "en",
                    summary = "Allowed only under the restrictions of Annex III of the EU Cosmetics Regulation."
                ),
                CosingCommentRecord(
                    locale = "pl",
                    summary = "Dozwolona wyłącznie z ograniczeniami załącznika III rozporządzenia kosmetycznego UE."
                )
            )
            else -> emptyList()
        }
    }

    private fun uniqueId(base: String): String {
        val count: Int = usedIds.merge(base, 1, Int::plus) ?: 1
        return if (count == 1) base else "$base-$count"
    }

    private fun slug(name: String): String {
        val normalized: String = name.lowercase()
            .replace(NON_ALNUM, "-")
            .trim('-')
            .replace(DASHES, "-")
        return normalized.ifEmpty { "unnamed" }
    }

    private companion object {
        val NON_ALNUM: Regex = Regex("[^a-z0-9]+")
        val DASHES: Regex = Regex("-{2,}")
        val COMMA_IN_NAME: Regex = Regex("\\d,\\d")
    }
}
