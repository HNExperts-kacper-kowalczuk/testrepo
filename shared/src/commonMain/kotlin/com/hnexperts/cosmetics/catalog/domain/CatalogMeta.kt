package com.hnexperts.cosmetics.catalog.domain

data class CatalogMeta(
    val catalogVersion: String,
    val rulesetVersion: String,
    val builtAt: String,
    val region: String,
    val checksum: String,
    val supportedCommentLocales: List<String>
)
