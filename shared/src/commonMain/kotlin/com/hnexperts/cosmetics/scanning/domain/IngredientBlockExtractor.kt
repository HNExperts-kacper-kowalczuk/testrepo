package com.hnexperts.cosmetics.scanning.domain

/**
 * Cuts an OCR transcript down to the printed ingredient list.
 *
 * The block starts after the last labelled header (Ingredients / INCI /
 * Skład / Inhaltsstoffe / …) and ends before the next labelled section
 * (Directions / Sposób użycia / …). When no header is present the whole
 * text is used: a tight user crop is itself the statement of where the
 * list is. Lines that are only digits, volumes, or barcodes are dropped.
 */
object IngredientBlockExtractor {
    fun extract(rawText: String): String {
        val text: String = rawText.trim()
        if (text.isEmpty()) {
            return ""
        }
        val content: String = afterLastStartMarker(text) ?: text
        return keepUntilStopMarker(content)
    }

    private fun afterLastStartMarker(text: String): String? {
        val match: MatchResult = START.findAll(text).lastOrNull() ?: return null
        val content: String = text.substring(match.range.last + 1)
        return content.ifBlank { null }
    }

    private fun keepUntilStopMarker(content: String): String {
        val kept: MutableList<String> = mutableListOf()
        for (line in content.split('\n')) {
            val trimmed: String = line.trim()
            if (STOP_LINE.containsMatchIn(trimmed)) {
                break
            }
            val inlineStop: MatchResult? = STOP_INLINE.find(trimmed)
            if (inlineStop != null) {
                appendIfUseful(kept, trimmed.substring(0, inlineStop.range.first))
                break
            }
            appendIfUseful(kept, trimmed)
        }
        return kept.joinToString(separator = "\n").trim()
    }

    private fun appendIfUseful(kept: MutableList<String>, line: String) {
        val trimmed: String = line.trim()
        if (trimmed.isEmpty() || JUNK_LINE.matches(trimmed)) {
            return
        }
        kept.add(trimmed)
    }

    private const val START_MARKERS: String =
        "ingredient list|ingredients?|inci|composition|" +
            "sk[łl]ad(?:niki)?(?:[ \\t]+inci)?|" +
            "inhaltsstoffe|zutaten|ingr[eé]dients?|ingredientes|ingredienti"

    private const val STOP_MARKERS: String =
        "directions|how to use|usage|warnings?|caution|store in|made in|" +
            "manufactured|manufacturer|distributed by|best before|batch|pao|" +
            "spos[óo]b u[żz]ycia|ostrze[żz]enia|przechowywa[ćc]|wyprodukowano|" +
            "dystrybutor|pojemno[śs][ćc]|termin przydatno[śs]ci|partia|" +
            "najlepiej zu[żz]y[ćc]|anwendung|warnhinweise?|hergestellt|" +
            "mode d'emploi|pr[ée]cautions"

    private val START: Regex = Regex(
        "(?:^|\\n)[^\\p{L}\\n]{0,4}(?:$START_MARKERS)\\b" +
            "(?:[ \\t]*/[ \\t]*(?:$START_MARKERS)\\b)*" +
            "(?:[ \\t]*\\((?:$START_MARKERS)\\))?" +
            "[ \\t]*[:\\-–]?[ \\t]*",
        RegexOption.IGNORE_CASE
    )

    private val STOP_LINE: Regex = Regex(
        "^[^\\p{L}\\n]{0,4}(?:$STOP_MARKERS)\\b",
        RegexOption.IGNORE_CASE
    )

    private val STOP_INLINE: Regex = Regex(
        "[.。][ \\t]*(?:$STOP_MARKERS)[ \\t]*:",
        RegexOption.IGNORE_CASE
    )

    private val JUNK_LINE: Regex = Regex(
        "^[\\d\\s.,:;%/-]+$|^\\d{8,14}$|^\\d+(?:[.,]\\d+)?[ \\t]*(?:ml|g|kg|l|oz|fl\\.? ?oz\\.?)\\.?$",
        RegexOption.IGNORE_CASE
    )
}
