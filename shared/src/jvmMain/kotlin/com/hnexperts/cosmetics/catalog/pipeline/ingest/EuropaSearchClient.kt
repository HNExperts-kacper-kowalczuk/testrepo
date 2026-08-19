package com.hnexperts.cosmetics.catalog.pipeline.ingest

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Enumerates the CosIng inventory through the public Europa Search API used
 * by the CosIng web app. The API caps any single query at 10 000 results
 * (100 pages x 100), so the full ~36k inventory is fetched as a union of
 * token-prefix segments ("aa*".."99*") plus exact single-character tokens,
 * deduplicated by substance reference.
 */
class EuropaSearchClient(
    private val http: IngestHttp,
    private val apiKey: String = PUBLIC_COSING_API_KEY
) {
    private val json: Json = Json { ignoreUnknownKeys = true }

    fun fetchAllMetadata(onProgress: (String, Int) -> Unit = { _, _ -> }): List<JsonObject> {
        val seen: LinkedHashMap<String, JsonObject> = LinkedHashMap()
        for (segment in segments()) {
            val added: Int = fetchSegment(segment, seen)
            onProgress(segment, added)
        }
        return seen.values.toList()
    }

    private fun fetchSegment(segment: String, seen: LinkedHashMap<String, JsonObject>): Int {
        var page = 1
        var added = 0
        while (page <= MAX_PAGES) {
            val body: JsonObject = search(segment, page)
            val results = body["results"]?.jsonArray ?: break
            if (results.isEmpty()) {
                break
            }
            for (result in results) {
                val metadata: JsonObject = result.jsonObject["metadata"]?.jsonObject ?: continue
                val key: String = dedupeKey(metadata) ?: continue
                if (seen.putIfAbsent(key, metadata) == null) {
                    added++
                }
            }
            val total: Int = body["totalResults"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            if (total > MAX_PAGES * PAGE_SIZE && page == MAX_PAGES) {
                System.err.println("WARN: segment '$segment' exceeds the API depth cap ($total results)")
            }
            if (page * PAGE_SIZE >= total) {
                break
            }
            page++
        }
        return added
    }

    private fun search(text: String, page: Int): JsonObject {
        val encoded: String = URLEncoder.encode(text, StandardCharsets.UTF_8)
        val url = "$SEARCH_URL?apiKey=$apiKey&text=$encoded&pageSize=$PAGE_SIZE&pageNumber=$page"
        return json.parseToJsonElement(http.postText(url)).jsonObject
    }

    private fun dedupeKey(metadata: JsonObject): String? {
        val substanceId: String? = metadata.firstString("substanceId")
        if (substanceId != null) {
            return "id:$substanceId"
        }
        return metadata.firstString("reference")?.let { reference -> "ref:$reference" }
    }

    private fun segments(): List<String> {
        val alphabet: List<Char> = ('a'..'z') + ('0'..'9')
        val singles: List<String> = alphabet.map { first -> first.toString() }
        val pairs: List<String> = alphabet.flatMap { first ->
            alphabet.map { second -> "$first$second*" }
        }
        return singles + pairs
    }

    companion object {
        /** Published in the CosIng web app's env-json-config.json; not a secret. */
        const val PUBLIC_COSING_API_KEY: String = "285a77fd-1257-4271-8507-f0c6b2961203"
        private const val SEARCH_URL: String = "https://webgate.ec.europa.eu/es/search-api/rest/search"
        private const val PAGE_SIZE: Int = 100
        private const val MAX_PAGES: Int = 100
    }
}

internal fun JsonObject.firstString(key: String): String? {
    val values = this[key]?.jsonArray ?: return null
    val first: String? = values.firstOrNull()?.jsonPrimitive?.content?.trim()
    return first?.ifEmpty { null }
}

internal fun JsonObject.allStrings(key: String): List<String> {
    val values = this[key]?.jsonArray ?: return emptyList()
    return values.mapNotNull { value -> value.jsonPrimitive.content.trim().ifEmpty { null } }
}
