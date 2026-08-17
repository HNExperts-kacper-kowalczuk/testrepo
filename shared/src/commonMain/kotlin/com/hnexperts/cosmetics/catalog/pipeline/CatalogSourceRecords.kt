package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import kotlinx.serialization.Serializable

@Serializable
data class CosingIngredientRecord(
    val id: String,
    val inciName: String,
    val casNumbers: String? = null,
    val aliases: List<String> = emptyList(),
    val commaException: Boolean = false,
    val dangerLevel: String,
    val regulatoryTags: List<String> = emptyList(),
    val functionTags: List<String> = emptyList(),
    val restriction: UsageRestriction? = null,
    val comments: List<CosingCommentRecord>
)

@Serializable
data class CosingCommentRecord(
    val locale: String,
    val summary: String,
    val detail: String? = null
)

@Serializable
data class CosingIngredientDump(
    val source: String = "cosing-derived",
    val region: String,
    val catalogVersion: String,
    val rulesetVersion: String,
    val builtAt: String,
    val ingredients: List<CosingIngredientRecord>
)

@Serializable
data class ObfProductRecord(
    val id: String,
    val name: String,
    val brand: String? = null,
    val category: String? = null,
    val inciRaw: String,
    val usage: String? = null,
    val source: String = "obf",
    val verified: Boolean = false,
    val gtins: List<String>
)

@Serializable
data class ObfProductDump(
    val source: String = "open-beauty-facts",
    val region: String,
    val products: List<ObfProductRecord>
)
