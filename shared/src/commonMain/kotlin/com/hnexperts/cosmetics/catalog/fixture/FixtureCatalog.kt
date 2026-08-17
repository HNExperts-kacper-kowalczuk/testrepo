package com.hnexperts.cosmetics.catalog.fixture

object FixtureCatalog {
    const val RULESET_VERSION: String = "2026.08-fixture"
    const val CATALOG_VERSION: String = "2026.08-fixture"

    val ingredients: List<FixtureIngredient>
        get() = FixtureIngredients.all

    val products: List<FixtureProduct>
        get() = FixtureProducts.all

    fun commaExceptions(): List<String> {
        return ingredients.filter { item -> item.commaException }.map { item -> item.ingredient.inciName }
    }

    fun aliasMap(): Map<String, String> {
        val result: MutableMap<String, String> = mutableMapOf()
        for (item in ingredients) {
            for (alias in item.aliases) {
                result[alias] = item.ingredient.id
            }
        }
        return result
    }
}
