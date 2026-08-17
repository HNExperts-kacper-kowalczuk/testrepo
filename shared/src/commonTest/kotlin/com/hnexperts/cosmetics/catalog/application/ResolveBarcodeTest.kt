package com.hnexperts.cosmetics.catalog.application

import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.failure.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class ResolveBarcodeTest {
    private val product: Product = Product(
        id = "gentle-cleanser",
        name = "Gentle Cream Cleanser",
        brand = "Fixture Care",
        category = "cleanser",
        inciRaw = "Aqua, Glycerin",
        usage = "RINSE_OFF",
        source = "curated",
        verified = true
    )
    private val resolveBarcode: ResolveBarcode = ResolveBarcode(
        MemoryProducts(mapOf("5901234123457" to product))
    )

    @Test
    fun rejectsShortValues() {
        runBlocking {
            val lookup: BarcodeLookup = requireOk(resolveBarcode.invoke("123"))
            assertIs<BarcodeLookup.Invalid>(lookup)
        }
    }

    @Test
    fun returnsNotFoundForUnknownGtin() {
        runBlocking {
            val lookup: BarcodeLookup = requireOk(resolveBarcode.invoke("5901234000000"))
            assertIs<BarcodeLookup.NotFound>(lookup)
            assertEquals("5901234000000", lookup.gtin)
        }
    }

    @Test
    fun findsProductAndStripsFormatting() {
        runBlocking {
            val lookup: BarcodeLookup = requireOk(resolveBarcode.invoke("590-1234-12345-7"))
            assertIs<BarcodeLookup.Found>(lookup)
            assertEquals("5901234123457", lookup.gtin)
            assertEquals("Gentle Cream Cleanser", lookup.product.name)
        }
    }

    private class MemoryProducts(
        private val byGtin: Map<String, Product>
    ) : ProductRepository {
        override suspend fun findByGtin(rawGtin: String): Outcome<Product?> {
            return Outcome.Ok(byGtin[rawGtin])
        }

        override suspend fun search(query: String): Outcome<List<Product>> {
            return Outcome.Ok(emptyList())
        }
    }

    private fun requireOk(outcome: Outcome<BarcodeLookup>): BarcodeLookup {
        assertTrue(outcome is Outcome.Ok)
        return outcome.value
    }
}
