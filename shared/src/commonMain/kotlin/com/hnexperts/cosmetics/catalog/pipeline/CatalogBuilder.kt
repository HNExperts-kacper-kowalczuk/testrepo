package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.catalog.application.CatalogManifest
import com.hnexperts.cosmetics.catalog.domain.CatalogIntegrity
import com.hnexperts.cosmetics.catalog.domain.CatalogMeta
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.fixture.FixtureIngredient
import com.hnexperts.cosmetics.catalog.fixture.FixtureProduct
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import com.hnexperts.cosmetics.ingredients.domain.Ingredient

data class CatalogBuild(
    val meta: CatalogMeta,
    val ingredients: List<FixtureIngredient>,
    val products: List<FixtureProduct>,
    val manifest: CatalogManifest
)

object CatalogBuilder {
    fun build(ingredientsDump: CosingIngredientDump, productsDump: ObfProductDump): CatalogBuild {
        val commentErrors: List<String> = CatalogCommentValidator().validate(ingredientsDump)
        if (commentErrors.isNotEmpty()) {
            throw IllegalStateException(commentErrors.joinToString(separator = "; "))
        }
        val ingredients: List<FixtureIngredient> = ingredientsDump.ingredients.map(::toFixture)
        val products: List<FixtureProduct> = productsDump.products.map(::toFixture)
        val checksum: String = CatalogIntegrity.fingerprint(
            catalogVersion = ingredientsDump.catalogVersion,
            rulesetVersion = ingredientsDump.rulesetVersion,
            builtAt = ingredientsDump.builtAt,
            region = ingredientsDump.region,
            ingredientIds = ingredients.map { item -> item.ingredient.id },
            productIds = products.map { item -> item.product.id }
        )
        val meta = CatalogMeta(
            catalogVersion = ingredientsDump.catalogVersion,
            rulesetVersion = ingredientsDump.rulesetVersion,
            builtAt = ingredientsDump.builtAt,
            region = ingredientsDump.region,
            checksum = checksum,
            supportedCommentLocales = listOf("en", "pl")
        )
        val manifest = CatalogManifest(
            catalogVersion = meta.catalogVersion,
            rulesetVersion = meta.rulesetVersion,
            builtAt = meta.builtAt,
            region = meta.region,
            checksum = meta.checksum,
            productCount = products.size,
            ingredientCount = ingredients.size
        )
        return CatalogBuild(
            meta = meta,
            ingredients = ingredients,
            products = products,
            manifest = manifest
        )
    }

    private fun toFixture(record: CosingIngredientRecord): FixtureIngredient {
        return FixtureIngredient(
            ingredient = Ingredient(
                id = record.id,
                inciName = record.inciName,
                casNumbers = record.casNumbers,
                functionTags = record.functionTags
            ),
            aliases = record.aliases,
            commaException = record.commaException,
            hazard = IngredientHazard(
                ingredientId = record.id,
                dangerLevel = DangerLevelParser.parse(record.dangerLevel),
                regulatoryTags = record.regulatoryTags,
                restrictionJson = record.restriction?.let { restriction -> UsageRestriction.toJson(restriction) }
            ),
            comments = record.comments.map { comment ->
                LocalizedText(locale = comment.locale, summary = comment.summary, detail = comment.detail)
            }
        )
    }

    private fun toFixture(record: ObfProductRecord): FixtureProduct {
        return FixtureProduct(
            product = Product(
                id = record.id,
                name = record.name,
                brand = record.brand,
                category = record.category,
                inciRaw = record.inciRaw,
                usage = record.usage,
                source = record.source,
                verified = record.verified
            ),
            gtins = record.gtins
        )
    }
}
