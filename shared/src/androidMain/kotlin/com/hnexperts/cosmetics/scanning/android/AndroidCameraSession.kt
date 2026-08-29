package com.hnexperts.cosmetics.scanning.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.toVerboseString
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.CameraPreviewLayout
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AndroidCameraSession(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onBarcode: (BarcodePayload) -> Unit,
    private val onStill: (CameraFrame) -> Unit,
    private val onFailure: (AppFailure) -> Unit
) {
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private val barcodeClient = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
    )
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastCaptureNonce: Int = 0
    private var previewView: PreviewView? = null
    private var boundMode: ScannerMode? = null
    private var pendingMode: ScannerMode? = null
    private var pendingTorch: Boolean = false
    private var layoutWaiter: View.OnLayoutChangeListener? = null
    @Volatile
    private var barcodeListening: Boolean = true

    fun attachPreview(view: PreviewView) {
        previewView = view
    }

    fun bindWhenReady(mode: ScannerMode, torchOn: Boolean) {
        val view: PreviewView = previewView ?: return
        pendingMode = mode
        pendingTorch = torchOn
        if (!CameraPreviewLayout.isReady(view.width, view.height)) {
            waitForLayout(view)
            return
        }
        clearLayoutWaiter(view)
        if (boundMode == mode && camera != null) {
            setTorch(torchOn)
            return
        }
        bind(mode, torchOn)
    }

    fun bind(mode: ScannerMode, torchOn: Boolean) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                bindToProvider(providerFuture.get(), mode, torchOn)
            } catch (error: Exception) {
                onFailure(AppFailure.Camera("camera.bind", error.toVerboseString()))
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setTorch(on: Boolean) {
        camera?.cameraControl?.enableTorch(on)
    }

    fun setBarcodeListening(listening: Boolean) {
        barcodeListening = listening
    }

    fun captureIfNeeded(nonce: Int) {
        if (nonce == 0 || nonce == lastCaptureNonce) {
            return
        }
        lastCaptureNonce = nonce
        val capture: ImageCapture = imageCapture ?: return
        capture.takePicture(analysisExecutor, stillCallback())
    }

    fun release() {
        previewView?.let(::clearLayoutWaiter)
        cameraProvider?.unbindAll()
        boundMode = null
        barcodeClient.close()
        analysisExecutor.shutdown()
    }

    private fun waitForLayout(view: PreviewView) {
        if (layoutWaiter != null) {
            return
        }
        val waiter = View.OnLayoutChangeListener { host, _, _, _, _, _, _, _, _ ->
            if (CameraPreviewLayout.isReady(host.width, host.height)) {
                val mode: ScannerMode = pendingMode ?: return@OnLayoutChangeListener
                bindWhenReady(mode, pendingTorch)
            }
        }
        layoutWaiter = waiter
        view.addOnLayoutChangeListener(waiter)
    }

    private fun clearLayoutWaiter(view: PreviewView) {
        val waiter: View.OnLayoutChangeListener = layoutWaiter ?: return
        view.removeOnLayoutChangeListener(waiter)
        layoutWaiter = null
    }

    private fun bindToProvider(provider: ProcessCameraProvider, mode: ScannerMode, torchOn: Boolean) {
        val view: PreviewView = previewView ?: return
        cameraProvider = provider
        provider.unbindAll()
        val preview: Preview = Preview.Builder().build().also { built ->
            built.setSurfaceProvider(view.surfaceProvider)
        }
        val capture: ImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCapture = capture
        val useCases = if (mode == ScannerMode.BARCODE) {
            arrayOf(preview, capture, barcodeAnalysis())
        } else {
            arrayOf(preview, capture)
        }
        camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, *useCases)
        boundMode = mode
        setTorch(torchOn)
    }

    private fun barcodeAnalysis(): ImageAnalysis {
        val analysis: ImageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val analyzer = MlKitAnalyzer(
            listOf(barcodeClient),
            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
            analysisExecutor
        ) { result ->
            if (!barcodeListening) {
                return@MlKitAnalyzer
            }
            val barcodes = result.getValue(barcodeClient).orEmpty()
            val payload: BarcodePayload = barcodes.firstNotNullOfOrNull(AndroidBarcodeMapper::toPayload) ?: return@MlKitAnalyzer
            mainHandler.post { onBarcode(payload) }
        }
        analysis.setAnalyzer(analysisExecutor, analyzer)
        return analysis
    }

    private fun stillCallback(): ImageCapture.OnImageCapturedCallback {
        return object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val frame = CameraFrame(
                        bytes = bytes,
                        width = image.width,
                        height = image.height,
                        rotationDegrees = image.imageInfo.rotationDegrees
                    )
                    mainHandler.post { onStill(frame) }
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onFailure(AppFailure.Camera("camera.capture", exception.toVerboseString()))
            }
        }
    }
}
