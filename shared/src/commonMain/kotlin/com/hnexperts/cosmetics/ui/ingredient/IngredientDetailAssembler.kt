package com.hnexperts.cosmetics.ui.ingredient

import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.evaluation.domain.Finding
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.IngredientHazard
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.hazards.domain.UsageRestriction
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.ingredients.domain.MatchMethod

object IngredientDetailAssembler {
    fun fromFinding(
        finding: Finding,
        index: CatalogIndex,
        comment: LocalizedText?
    ): IngredientDetail {
        val ingredientId: String? = finding.ingredient.id
        return assemble(
            source = DetailSource(
                title = finding.ingredient.displayName,
                ingredient = ingredientId?.let { id -> index.ingredientsById[id] },
                hazard = ingredientId?.let { id -> index.hazardsById[id] },
                comment = comment,
                level = finding.level,
                regulatoryTags = finding.regulatoryTags,
                matchMethod = finding.ingredient.matchedBy,
                listPosition = finding.listIndex + 1,
                sunCaution = finding.sunCaution(),
                earlyList = finding.earlyListConcern(),
                usageAdjusted = finding.usageAdjusted,
                personalAvoid = finding.personalAvoid,
                unmatched = isUnmatched(ingredientId, finding.ingredient.matchedBy),
                aliases = aliasesOf(ingredientId, index)
            )
        )
    }

    fun fromCatalogIngredient(
        ingredient: Ingredient,
        index: CatalogIndex,
        comment: LocalizedText?,
        level: DangerLevel?
    ): IngredientDetail {
        val hazard: IngredientHazard? = index.hazardsById[ingredient.id]
        return assemble(
            source = DetailSource(
                title = ingredient.inciName,
                ingredient = ingredient,
                hazard = hazard,
                comment = comment,
                level = level ?: hazard?.dangerLevel,
                regulatoryTags = hazard?.regulatoryTags.orEmpty(),
                matchMethod = null,
                listPosition = null,
                sunCaution = hazard?.regulatoryTags.orEmpty().contains(TAG_PHOTOTOXIC),
                earlyList = false,
                usageAdjusted = false,
                personalAvoid = false,
                unmatched = false,
                aliases = aliasesOf(ingredient.id, index)
            )
        )
    }

    private fun assemble(source: DetailSource): IngredientDetail {
        val ingredient: Ingredient? = source.ingredient
        return IngredientDetail(
            title = source.title,
            catalogInciName = catalogNameIfDifferent(source.title, ingredient?.inciName),
            casNumbers = blankToNull(ingredient?.casNumbers),
            level = source.level,
            summary = blankToNull(source.comment?.summary),
            detail = blankToNull(source.comment?.detail),
            aliases = source.aliases.filter { alias -> !alias.equals(source.title, ignoreCase = true) },
            functionTags = ingredient?.functionTags.orEmpty(),
            regulatoryTags = source.regulatoryTags,
            restriction = restrictionOf(source.hazard),
            matchMethod = source.matchMethod,
            listPosition = source.listPosition,
            sunCaution = source.sunCaution,
            earlyList = source.earlyList,
            usageAdjusted = source.usageAdjusted,
            personalAvoid = source.personalAvoid,
            unmatched = source.unmatched
        )
    }

    private fun aliasesOf(ingredientId: String?, index: CatalogIndex): List<String> {
        if (ingredientId == null) {
            return emptyList()
        }
        return index.aliasesFor(ingredientId)
    }

    private fun catalogNameIfDifferent(title: String, catalogName: String?): String? {
        val name: String = blankToNull(catalogName) ?: return null
        if (name.equals(title, ignoreCase = true)) {
            return null
        }
        return name
    }

    private fun restrictionOf(hazard: IngredientHazard?): UsageRestriction? {
        return UsageRestriction.fromJson(hazard?.restrictionJson)
    }

    private fun isUnmatched(ingredientId: String?, matchedBy: MatchMethod): Boolean {
        return ingredientId == null || matchedBy == MatchMethod.UNMATCHED
    }

    private fun blankToNull(value: String?): String? {
        val trimmed: String = value?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        return trimmed
    }

    private data class DetailSource(
        val title: String,
        val ingredient: Ingredient?,
        val hazard: IngredientHazard?,
        val comment: LocalizedText?,
        val level: DangerLevel?,
        val regulatoryTags: List<String>,
        val matchMethod: MatchMethod?,
        val listPosition: Int?,
        val sunCaution: Boolean,
        val earlyList: Boolean,
        val usageAdjusted: Boolean,
        val personalAvoid: Boolean,
        val unmatched: Boolean,
        val aliases: List<String>
    )

    private const val TAG_PHOTOTOXIC: String = "PHOTOTOXIC"
}
