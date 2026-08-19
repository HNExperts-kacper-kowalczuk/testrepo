package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class OnlineGtinHit {
    data class WithIngredients(
        val gtin: String,
        val name: String,
        val brand: String?,
        val inciRaw: String,
        val usage: ProductUsage
    ) : OnlineGtinHit()

    data class MissingIngredients(val gtin: String, val name: String?) : OnlineGtinHit()

    data class NotFound(val gtin: String) : OnlineGtinHit()
}

/**
 * Maps an Open Beauty Facts / Open Food Facts v2 product JSON body.
 * Network I/O lives in [OnlineGtinLookup]; this object is pure and unit-tested.
 */
object ObfProductParser {
    private val json: Json = Json { ignoreUnknownKeys = true }

    fun parse(gtin: String, body: String): OnlineGtinHit {
        val root: JsonObject = try {
            json.parseToJsonElement(body).jsonObject
        } catch (invalid: Exception) {
            return OnlineGtinHit.NotFound(gtin)
        }
        val status: Int = root["status"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        val product: JsonObject = root["product"]?.jsonObject
            ?: return OnlineGtinHit.NotFound(gtin)
        if (status != 1 && product.isEmpty()) {
            return OnlineGtinHit.NotFound(gtin)
        }
        val inci: String? = bestInci(product)
        val name: String? = stringField(product, "product_name")
            ?: stringField(product, "generic_name")
            ?: stringField(product, "product_name_en")
        if (inci == null) {
            return OnlineGtinHit.MissingIngredients(gtin, name)
        }
        return OnlineGtinHit.WithIngredients(
            gtin = gtin,
            name = name ?: gtin,
            brand = stringField(product, "brands")?.split(',')?.first()?.trim(),
            inciRaw = inci,
            usage = usageFor(product)
        )
    }

    private fun bestInci(product: JsonObject): String? {
        val preferred: List<String> = listOf("ingredients_text_en", "ingredients_text")
        for (key in preferred) {
            val value: String? = stringField(product, key)
            if (value != null && value.length >= MIN_INCI) {
                return value
            }
        }
        return product.entries
            .filter { entry -> entry.key.startsWith("ingredients_text") }
            .mapNotNull { entry -> stringField(product, entry.key) }
            .filter { text -> text.length >= MIN_INCI }
            .maxByOrNull { text -> text.length }
    }

    private fun usageFor(product: JsonObject): ProductUsage {
        val tags: String = buildString {
            append(stringField(product, "categories").orEmpty())
            append(' ')
            val array = product["categories_tags"]?.jsonArray
            if (array != null) {
                append(array.joinToString(" ") { element -> element.jsonPrimitive.content })
            }
        }.lowercase()
        return when {
            RINSE.containsMatchIn(tags) -> ProductUsage.RINSE_OFF
            SPRAY.containsMatchIn(tags) -> ProductUsage.SPRAY
            LIP.containsMatchIn(tags) -> ProductUsage.LIP
            EYE.containsMatchIn(tags) -> ProductUsage.EYE
            else -> ProductUsage.LEAVE_ON
        }
    }

    private fun stringField(obj: JsonObject, key: String): String? {
        val element = obj[key] ?: return null
        val value: String = try {
            element.jsonPrimitive.content.trim()
        } catch (notText: Exception) {
            return null
        }
        return value.ifEmpty { null }
    }

    private const val MIN_INCI: Int = 20
    private val RINSE: Regex = Regex("shampoo|shower|soap|bath|cleanser|wash|rinse|conditioner")
    private val SPRAY: Regex = Regex("deodorant|antiperspirant|perfume|eau-de|mist|spray")
    private val LIP: Regex = Regex("lipstick|lip-|lips\\b|balm")
    private val EYE: Regex = Regex("mascara|eyeliner|eye-shadow|eyeshadow|eye-cream")
}
