package com.hnexperts.cosmetics.ui.camera

import com.hnexperts.cosmetics.ads.domain.NetworkMonitor
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.application.OnlineGtinLookup
import com.hnexperts.cosmetics.catalog.application.ResolveBarcode
import com.hnexperts.cosmetics.catalog.application.ResolveGtin
import com.hnexperts.cosmetics.catalog.domain.CachedOnlineProduct
import com.hnexperts.cosmetics.catalog.domain.OnlineProductCache
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.network.SimpleHttpClient
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.scanning.application.PendingCaptureSession
import com.hnexperts.cosmetics.scanning.application.PendingVerifySession
import com.hnexperts.cosmetics.scanning.application.ScanBridge
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPermissionStatus
import com.hnexperts.cosmetics.scanning.domain.CatalogReport
import com.hnexperts.cosmetics.scanning.domain.HistoryEntry
import com.hnexperts.cosmetics.scanning.domain.ReportQueue
import com.hnexperts.cosmetics.scanning.domain.ScanHistoryRepository
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CameraScanViewModelGalleryTest {
    @Test
    fun galleryStillPublishesFrameAndOpensCropWithoutCameraPermission() {
        val pending: PendingCaptureSession = PendingCaptureSession()
        val viewModel: CameraScanViewModel = cameraScanViewModel(pending, ScannerMode.INGREDIENT_LIST)
        viewModel.onPermission(CameraPermissionStatus.DENIED)
        val frame: CameraFrame = sampleFrame()
        viewModel.onGalleryStill(frame)
        assertSame(frame, pending.peek())
        assertTrue(viewModel.uiState.value.navigateToCrop)
        assertNull(viewModel.uiState.value.failure)
        assertEquals(0, viewModel.uiState.value.captureNonce)
    }

    @Test
    fun galleryStillIsIgnoredInBarcodeMode() {
        val pending: PendingCaptureSession = PendingCaptureSession()
        val viewModel: CameraScanViewModel = cameraScanViewModel(pending, ScannerMode.BARCODE)
        viewModel.onGalleryStill(sampleFrame())
        assertNull(pending.peek())
        assertFalse(viewModel.uiState.value.navigateToCrop)
    }

    @Test
    fun galleryStillEmptySetsOcrGalleryFailure() {
        val viewModel: CameraScanViewModel = cameraScanViewModel(
            pending = PendingCaptureSession(),
            mode = ScannerMode.INGREDIENT_LIST
        )
        viewModel.onGalleryStillEmpty()
        val failure: AppFailure.Camera = assertIs(viewModel.uiState.value.failure)
        assertEquals("ocr.gallery", failure.operation)
        assertFalse(viewModel.uiState.value.navigateToCrop)
    }

    @Test
    fun barcodeGalleryEmptyKeepsBarcodeOperation() {
        val viewModel: CameraScanViewModel = cameraScanViewModel(
            pending = PendingCaptureSession(),
            mode = ScannerMode.BARCODE
        )
        viewModel.onGalleryEmpty()
        val failure: AppFailure.Camera = assertIs(viewModel.uiState.value.failure)
        assertEquals("barcode.gallery", failure.operation)
    }

    @Test
    fun captureStillRequiresCameraPermission() {
        val viewModel: CameraScanViewModel = cameraScanViewModel(
            pending = PendingCaptureSession(),
            mode = ScannerMode.INGREDIENT_LIST
        )
        viewModel.onPermission(CameraPermissionStatus.DENIED)
        viewModel.captureStill()
        assertEquals(0, viewModel.uiState.value.captureNonce)
    }

    private fun sampleFrame(): CameraFrame {
        return CameraFrame(bytes = byteArrayOf(1, 2, 3), width = 8, height = 12, rotationDegrees = 0)
    }

    private fun cameraScanViewModel(
        pending: PendingCaptureSession,
        mode: ScannerMode
    ): CameraScanViewModel {
        return CameraScanViewModel(
            resolveGtin = unusedResolveGtin(),
            evaluateProduct = unusedEvaluateProduct(),
            pendingCapture = pending,
            scanBridge = ScanBridge(),
            reports = UnusedReports,
            pendingVerify = PendingVerifySession(),
            initialMode = mode
        )
    }

    private fun unusedResolveGtin(): ResolveGtin {
        return ResolveGtin(
            offline = ResolveBarcode(UnusedProducts),
            cache = UnusedCache,
            online = OnlineGtinLookup(UnusedHttp, OfflineNetwork)
        )
    }

    private fun unusedEvaluateProduct(): EvaluateProduct {
        return EvaluateProduct(
            catalog = UnusedCatalog,
            preferences = UnusedPreferences,
            history = UnusedHistory,
            session = EvaluationSession(),
            dispatchers = AppDispatchers()
        )
    }

    private object UnusedReports : ReportQueue {
        override suspend fun enqueue(report: CatalogReport): Outcome<Unit> = Outcome.Ok(Unit)
        override suspend fun attachPayload(gtin: String, kind: String, payloadJson: String): Outcome<Unit> = Outcome.Ok(Unit)
        override suspend fun openCount(): Outcome<Long> = Outcome.Ok(0)
        override suspend fun openReports(): Outcome<List<CatalogReport>> = Outcome.Ok(emptyList())
        override suspend fun markAllOpenFlushed(): Outcome<Unit> = Outcome.Ok(Unit)
        override suspend fun clear(): Outcome<Unit> = Outcome.Ok(Unit)
    }

    private object UnusedProducts : ProductRepository {
        override suspend fun findByGtin(rawGtin: String): Outcome<Product?> = Outcome.Ok(null)
        override suspend fun findById(productId: String): Outcome<Product?> = Outcome.Ok(null)
        override suspend fun search(query: String): Outcome<List<Product>> = Outcome.Ok(emptyList())
        override suspend fun findByCategory(category: String, limit: Int): Outcome<List<Product>> = Outcome.Ok(emptyList())
        override suspend fun frequentCategories(limit: Int): Outcome<List<String>> = Outcome.Ok(emptyList())
    }

    private object UnusedCache : OnlineProductCache {
        override suspend fun find(gtin: String): Outcome<CachedOnlineProduct?> = Outcome.Ok(null)
        override suspend fun put(product: CachedOnlineProduct): Outcome<Unit> = Outcome.Ok(Unit)
        override suspend fun clear(): Outcome<Unit> = Outcome.Ok(Unit)
    }

    private object UnusedHttp : SimpleHttpClient {
        override suspend fun getText(url: String): Outcome<String> {
            return Outcome.Err(AppFailure.Network(operation = "test.http", detail = "unused"))
        }
    }

    private object OfflineNetwork : NetworkMonitor {
        override fun isOnline(): Boolean = false
    }

    private object UnusedCatalog : CatalogGateway {
        override suspend fun awaitIndex(): Outcome<CatalogIndex> {
            return Outcome.Err(AppFailure.CatalogLoad(operation = "test.catalog", detail = "unused"))
        }
    }

    private object UnusedPreferences : PreferencesStore {
        override suspend fun load(): Outcome<StoredPreferences> {
            return Outcome.Err(AppFailure.Database(operation = "test.prefs", detail = "unused"))
        }

        override suspend fun save(preferences: StoredPreferences): Outcome<Unit> = Outcome.Ok(Unit)
    }

    private object UnusedHistory : ScanHistoryRepository {
        override suspend fun record(assessment: ProductAssessment, source: String): Outcome<Unit> = Outcome.Ok(Unit)
        override suspend fun recent(): Outcome<List<HistoryEntry>> = Outcome.Ok(emptyList())
        override suspend fun clear(): Outcome<Unit> = Outcome.Ok(Unit)
    }
}
