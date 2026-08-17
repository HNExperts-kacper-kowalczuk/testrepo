package com.hnexperts.cosmetics.catalog.pipeline

import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.DangerLevelParser

class CatalogCommentValidator(
    private val requiredLocales: List<String> = listOf("en", "pl")
) {
    fun validate(dump: CosingIngredientDump): List<String> {
        val errors: MutableList<String> = mutableListOf()
        for (ingredient in dump.ingredients) {
            val level: DangerLevel = DangerLevelParser.parse(ingredient.dangerLevel)
            if (!requiresFullComments(level)) {
                continue
            }
            val locales: Set<String> = ingredient.comments
                .filter { comment -> comment.summary.isNotBlank() }
                .map { comment -> comment.locale }
                .toSet()
            for (required in requiredLocales) {
                if (!locales.contains(required)) {
                    errors.add(
                        "ingredient '${ingredient.id}' at $level is missing a '$required' comment"
                    )
                }
            }
        }
        return errors
    }

    private fun requiresFullComments(level: DangerLevel): Boolean {
        return level == DangerLevel.HIGH || level == DangerLevel.PROHIBITED
    }
}
