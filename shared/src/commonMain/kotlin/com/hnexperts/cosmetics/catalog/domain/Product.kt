package com.hnexperts.cosmetics.catalog.domain

data class Product(
    val id: String,
    val name: String,
    val brand: String?,
    val category: String?,
    val inciRaw: String,
    val usage: String?,
    val source: String,
    val verified: Boolean
)
