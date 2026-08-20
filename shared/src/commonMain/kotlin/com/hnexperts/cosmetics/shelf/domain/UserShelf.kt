package com.hnexperts.cosmetics.shelf.domain

import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.Outcome

data class ShelfItem(
    val shelfKey: String,
    val productId: String?,
    val gtin: String?,
    val name: String?,
    val brand: String?,
    val inciRaw: String,
    val rating: String,
    val usage: ProductUsage,
    val category: String? = null,
    val savedAt: String
)

interface UserShelf {
    suspend fun all(): Outcome<List<ShelfItem>>
    suspend fun contains(shelfKey: String): Outcome<Boolean>
    suspend fun save(item: ShelfItem): Outcome<Unit>
    suspend fun remove(shelfKey: String): Outcome<Unit>
}

object ShelfKeys {
    fun of(assessment: ProductAssessment): String {
        return of(gtin = assessment.gtin, productId = assessment.productId, inciRaw = assessment.inciRaw)
    }

    fun of(gtin: String?, productId: String?, inciRaw: String): String {
        if (!gtin.isNullOrBlank()) {
            return "gtin:$gtin"
        }
        if (!productId.isNullOrBlank()) {
            return "id:$productId"
        }
        return "inci:$inciRaw"
    }
}
