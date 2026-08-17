package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.domain.CameraFrame
import com.hnexperts.cosmetics.scanning.domain.ScannerMode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import platform.AVFoundation.AVCaptureDevice.Companion.defaultDeviceWithMediaType
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
class IosCameraSession(
    private val onBarcode: (BarcodePayload) -> Unit,
    private val onStill: (CameraFrame) -> Unit,
    private val onFailure: (AppFailure) -> Unit
) {
    private val session: AVCaptureSession = AVCaptureSession()
    private val photoOutput: AVCapturePhotoOutput = AVCapturePhotoOutput()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var lastNonce: Int = 0
    private val metadataDelegate = MetadataDelegate(
        onBarcode = onBarcode,
        listening = { barcodeListening }
    )
    private val photoDelegate = PhotoDelegate(onStill, onFailure)
    private var started: Boolean = false
    @Volatile
    private var barcodeListening: Boolean = true

    fun attach(view: UIView) {
        session.sessionPreset = AVCaptureSessionPresetPhoto
        val device: AVCaptureDevice = defaultDeviceWithMediaType(AVMediaTypeVideo)
            ?: run {
                onFailure(AppFailure.Camera("camera.device", "No video capture device is available"))
                return
            }
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
        }
        val metadata = AVCaptureMetadataOutput()
        if (session.canAddOutput(metadata)) {
            session.addOutput(metadata)
            metadata.setMetadataObjectsDelegate(metadataDelegate, dispatch_get_main_queue())
            metadata.metadataObjectTypes = listOf(
                AVMetadataObjectTypeEAN13Code,
                AVMetadataObjectTypeEAN8Code,
                AVMetadataObjectTypeUPCECode
            )
        }
        if (session.canAddOutput(photoOutput)) {
            session.addOutput(photoOutput)
        }
        val layer = AVCaptureVideoPreviewLayer(session = session)
        layer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.frame = view.bounds
        view.layer.addSublayer(layer)
        previewLayer = layer
    }

    fun start() {
        if (started) {
            return
        }
        started = true
        session.startRunning()
    }

    fun layout(view: UIView) {
        previewLayer?.frame = view.bounds
    }

    fun setTorch(on: Boolean) {
        val device: AVCaptureDevice = defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        if (!device.hasTorch) {
            return
        }
        device.lockForConfiguration(null)
        device.torchMode = if (on) AVCaptureTorchModeOn else AVCaptureTorchModeOff
        device.unlockForConfiguration()
    }

    fun setBarcodeListening(listening: Boolean) {
        barcodeListening = listening
    }

    fun captureIfNeeded(nonce: Int) {
        if (nonce == 0 || nonce == lastNonce) {
            return
        }
        lastNonce = nonce
        photoOutput.capturePhotoWithSettings(AVCapturePhotoSettings.photoSettings(), photoDelegate)
    }

    fun release() {
        session.stopRunning()
        previewLayer?.removeFromSuperlayer()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class MetadataDelegate(
    private val onBarcode: (BarcodePayload) -> Unit,
    private val listening: () -> Boolean
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        if (!listening()) {
            return
        }
        val code = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject ?: return
        val payload: BarcodePayload = IosBarcodeMapper.toPayload(code) ?: return
        onBarcode(payload)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PhotoDelegate(
    private val onStill: (CameraFrame) -> Unit,
    private val onFailure: (AppFailure) -> Unit
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) {
            onFailure(AppFailure.Camera("camera.capture", error.localizedDescription))
            return
        }
        val data: NSData = didFinishProcessingPhoto.fileDataRepresentation() ?: run {
            onFailure(AppFailure.Camera("camera.capture", "Photo capture returned no JPEG data"))
            return
        }
        onStill(
            CameraFrame(
                bytes = data.toByteArray(),
                width = 0,
                height = 0,
                rotationDegrees = 0
            )
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size: Int = length.toInt()
    val result = ByteArray(size)
    if (size == 0) {
        return result
    }
    memcpyBytes(result, this)
    return result
}

@OptIn(ExperimentalForeignApi::class)
private fun memcpyBytes(target: ByteArray, data: NSData) {
    val pointer = data.bytes ?: return
    target.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), pointer, data.length)
    }
}
