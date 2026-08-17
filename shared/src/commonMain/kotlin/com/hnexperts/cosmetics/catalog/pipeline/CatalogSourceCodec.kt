package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.fixture.FixtureCatalog
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import kotlinx.serialization.json.Json

object CatalogSourceCodec {
    val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun encodeIngredients(): String {
        val dump = CosingIngredientDump(
            region = "EU",
            catalogVersion = FixtureCatalog.CATALOG_VERSION,
            rulesetVersion = FixtureCatalog.RULESET_VERSION,
            builtAt = com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity.FIXTURE_BUILT_AT,
            ingredients = FixtureCatalog.ingredients.map(::toRecord)
        )
        return json.encodeToString(CosingIngredientDump.serializer(), dump)
    }

    fun encodeProducts(): String {
        val dump = ObfProductDump(
            region = "EU",
            products = FixtureCatalog.products.map(::toRecord)
        )
        return json.encodeToString(ObfProductDump.serializer(), dump)
    }

    fun parseIngredients(raw: String): CosingIngredientDump {
        return json.decodeFromString(CosingIngredientDump.serializer(), raw)
    }

    fun parseProducts(raw: String): ObfProductDump {
        return json.decodeFromString(ObfProductDump.serializer(), raw)
    }

    private fun toRecord(item: FixtureIngredient): CosingIngredientRecord {
        val restriction: UsageRestriction? = UsageRestriction.fromJson(item.hazard.restrictionJson)
        return CosingIngredientRecord(
            id = item.ingredient.id,
            inciName = item.ingredient.inciName,
            casNumbers = item.ingredient.casNumbers,
            aliases = item.aliases,
            commaException = item.commaException,
            dangerLevel = item.hazard.dangerLevel.name,
            regulatoryTags = item.hazard.regulatoryTags,
            functionTags = item.ingredient.functionTags,
            restriction = restriction,
            comments = item.comments.map { comment ->
                CosingCommentRecord(
                    locale = comment.locale,
                    summary = comment.summary,
                    detail = comment.detail
                )
            }
        )
    }

    private fun toRecord(item: FixtureProduct): ObfProductRecord {
        return ObfProductRecord(
            id = item.product.id,
            name = item.product.name,
            brand = item.product.brand,
            category = item.product.category,
            inciRaw = item.product.inciRaw,
            usage = item.product.usage,
            source = item.product.source,
            verified = item.product.verified,
            gtins = item.gtins
        )
    }
}
