package com.hnexperts.cosmetics.preferences.application

import com.hnexperts.cosmetics.catalog.domain.CachedOnlineProduct
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.preferences.domain.ThemePreference
import com.hnexperts.cosmetics.preferences.domain.UserAvoidanceProfile
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.shelf.domain.UserShelf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class UserDataResetTest {
    @Test
    fun clearShelfLeavesHistory() = runBlocking {
        val history = MemoryHistory()
        val shelf = MemoryShelf()
        history.entries += sampleHistory()
        shelf.items += sampleShelf()
        val reset = UserDataReset(history, shelf, MemoryReports(), MemoryCache(), MemoryPreferences())

        val result: Outcome<Unit> = reset.clearShelf()

        assertTrue(result is Outcome.Ok)
        assertTrue(shelf.items.isEmpty())
        assertEquals(1, history.entries.size)
    }

    @Test
    fun clearAvoidListKeepsPresetsAndLocale() = runBlocking {
        val preferences = MemoryPreferences(
            stored = StoredPreferences(
                profile = UserAvoidanceProfile(
                    pregnancyCaution = true,
                    fragranceFree = true,
                    avoidedIngredientIds = setOf("parfum"),
                    euAllergens = true
                ),
                localePreference = LocalePreference.PINNED,
                pinnedLocale = AppLocale.POLISH,
                adsRemoved = true
            )
        )
        val reset = UserDataReset(MemoryHistory(), MemoryShelf(), MemoryReports(), MemoryCache(), preferences)

        val result: Outcome<Unit> = reset.clearAvoidList()

        assertTrue(result is Outcome.Ok)
        assertTrue(preferences.stored.profile.avoidedIngredientIds.isEmpty())
        assertTrue(preferences.stored.profile.pregnancyCaution)
        assertTrue(preferences.stored.profile.fragranceFree)
        assertTrue(preferences.stored.profile.euAllergens)
        assertEquals(LocalePreference.PINNED, preferences.stored.localePreference)
        assertEquals(AppLocale.POLISH, preferences.stored.pinnedLocale)
        assertTrue(preferences.stored.adsRemoved)
    }

    @Test
    fun resetDeviceClearsUserStoresAndKeepsLocaleAndTheme() = runBlocking {
        val history = MemoryHistory()
        val shelf = MemoryShelf()
        val reports = MemoryReports()
        val cache = MemoryCache()
        val preferences = MemoryPreferences(
            stored = StoredPreferences(
                profile = UserAvoidanceProfile(
                    pregnancyCaution = true,
                    fragranceFree = true,
                    avoidedIngredientIds = setOf("parfum"),
                    euAllergens = true,
                    childrenCaution = true,
                    alcoholLeaveOn = true,
                    essentialOilCluster = true
                ),
                localePreference = LocalePreference.PINNED,
                pinnedLocale = AppLocale.POLISH,
                adsRemoved = true,
                themePreference = ThemePreference.DARK
            )
        )
        history.entries += sampleHistory()
        shelf.items += sampleShelf()
        reports.items += CatalogReport(kind = "missing_product", gtin = "590", payloadJson = "{}")
        cache.items["590"] = sampleCached()
        val reset = UserDataReset(history, shelf, reports, cache, preferences)

        val result: Outcome<Unit> = reset.resetDevice()

        assertTrue(result is Outcome.Ok)
        assertTrue(history.entries.isEmpty())
        assertTrue(shelf.items.isEmpty())
        assertTrue(reports.items.isEmpty())
        assertTrue(cache.items.isEmpty())
        assertEquals(UserAvoidanceProfile.EMPTY, preferences.stored.profile)
        assertFalse(preferences.stored.adsRemoved)
        assertEquals(LocalePreference.PINNED, preferences.stored.localePreference)
        assertEquals(AppLocale.POLISH, preferences.stored.pinnedLocale)
        assertEquals(ThemePreference.DARK, preferences.stored.themePreference)
    }

    @Test
    fun resetDeviceStopsOnFirstFailure() = runBlocking {
        val history = MemoryHistory(failClear = true)
        val shelf = MemoryShelf()
        shelf.items += sampleShelf()
        val reset = UserDataReset(history, shelf, MemoryReports(), MemoryCache(), MemoryPreferences())

        val result: Outcome<Unit> = reset.resetDevice()

        assertTrue(result is Outcome.Err)
        assertEquals(1, shelf.items.size)
        assertEquals(0, shelf.clearCalls)
    }

    private fun sampleHistory(): HistoryEntry {
        return HistoryEntry(
            id = 1L,
            scannedAt = "2026-01-01T00:00:00Z",
            gtin = "5901234123457",
            productId = null,
            inciRaw = "Aqua",
            rating = "LOW",
            source = "barcode"
        )
    }

    private fun sampleShelf(): ShelfItem {
        return ShelfItem(
            shelfKey = "gtin:5901234123457",
            productId = null,
            gtin = "5901234123457",
            name = "Cleanser",
            brand = null,
            inciRaw = "Aqua",
            rating = "LOW",
            usage = ProductUsage.RINSE_OFF,
            savedAt = "2026-01-01T00:00:00Z"
        )
    }

    private fun sampleCached(): CachedOnlineProduct {
        return CachedOnlineProduct(
            gtin = "590",
            name = "Online cream",
            brand = null,
            inciRaw = "Aqua",
            usage = "UNKNOWN",
            source = "obf",
            cachedAt = "2026-01-01T00:00:00Z"
        )
    }

    private class MemoryHistory(
        private val failClear: Boolean = false
    ) : ScanHistoryRepository {
        val entries: MutableList<HistoryEntry> = mutableListOf()

        override suspend fun record(assessment: ProductAssessment, source: String): Outcome<Unit> {
            return Outcome.Ok(Unit)
        }

        override suspend fun recent(): Outcome<List<HistoryEntry>> {
            return Outcome.Ok(entries.toList())
        }

        override suspend fun clear(): Outcome<Unit> {
            if (failClear) {
                return Outcome.Err(AppFailure.Database(operation = "history.clear", detail = "locked"))
            }
            entries.clear()
            return Outcome.Ok(Unit)
        }
    }

    private class MemoryShelf : UserShelf {
        val items: MutableList<ShelfItem> = mutableListOf()
        var clearCalls: Int = 0

        override suspend fun all(): Outcome<List<ShelfItem>> {
            return Outcome.Ok(items.toList())
        }

        override suspend fun contains(shelfKey: String): Outcome<Boolean> {
            return Outcome.Ok(items.any { item -> item.shelfKey == shelfKey })
        }

        override suspend fun save(item: ShelfItem): Outcome<Unit> {
            items += item
            return Outcome.Ok(Unit)
        }

        override suspend fun remove(shelfKey: String): Outcome<Unit> {
            items.removeAll { item -> item.shelfKey == shelfKey }
            return Outcome.Ok(Unit)
        }

        override suspend fun clearAll(): Outcome<Unit> {
            clearCalls += 1
            items.clear()
            return Outcome.Ok(Unit)
        }
    }

    private class MemoryReports : ReportQueue {
        val items: MutableList<CatalogReport> = mutableListOf()

        override suspend fun enqueue(report: CatalogReport): Outcome<Unit> {
            items += report
            return Outcome.Ok(Unit)
        }

        override suspend fun attachPayload(gtin: String, kind: String, payloadJson: String): Outcome<Unit> {
            return Outcome.Ok(Unit)
        }

        override suspend fun openCount(): Outcome<Long> {
            return Outcome.Ok(items.size.toLong())
        }

        override suspend fun openReports(): Outcome<List<CatalogReport>> {
            return Outcome.Ok(items.toList())
        }

        override suspend fun markAllOpenFlushed(): Outcome<Unit> {
            items.clear()
            return Outcome.Ok(Unit)
        }

        override suspend fun clear(): Outcome<Unit> {
            items.clear()
            return Outcome.Ok(Unit)
        }
    }

    private class MemoryCache : OnlineProductCache {
        val items: MutableMap<String, CachedOnlineProduct> = mutableMapOf()

        override suspend fun find(gtin: String): Outcome<CachedOnlineProduct?> {
            return Outcome.Ok(items[gtin])
        }

        override suspend fun put(product: CachedOnlineProduct): Outcome<Unit> {
            items[product.gtin] = product
            return Outcome.Ok(Unit)
        }

        override suspend fun clear(): Outcome<Unit> {
            items.clear()
            return Outcome.Ok(Unit)
        }
    }

    private class MemoryPreferences(
        var stored: StoredPreferences = StoredPreferences(
            profile = UserAvoidanceProfile.EMPTY,
            localePreference = LocalePreference.FOLLOW_SYSTEM,
            pinnedLocale = null
        )
    ) : PreferencesStore {
        override suspend fun load(): Outcome<StoredPreferences> {
            return Outcome.Ok(stored)
        }

        override suspend fun save(preferences: StoredPreferences): Outcome<Unit> {
            stored = preferences
            return Outcome.Ok(Unit)
        }
    }
}
