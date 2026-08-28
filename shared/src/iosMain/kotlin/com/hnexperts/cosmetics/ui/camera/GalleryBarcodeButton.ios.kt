package com.hnexperts.cosmetics.ui.camera

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hnexperts.cosmetics.platform.iosRootViewController
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.camera_gallery
import com.hnexperts.cosmetics.scanning.domain.BarcodePayload
import com.hnexperts.cosmetics.scanning.ios.IosStillBarcodeDecoder
import org.jetbrains.compose.resources.stringResource
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun GalleryBarcodeButton(
    enabled: Boolean,
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    TextButton(
        onClick = { presentPicker(onBarcode = onBarcode, onEmpty = onEmpty, onCancel = onCancel) },
        enabled = enabled,
        modifier = modifier
    ) {
        Text(stringResource(Res.string.camera_gallery))
    }
}

private var retainedDelegate: GalleryPickerDelegate? = null

private fun presentPicker(
    onBarcode: (BarcodePayload) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit
) {
    val root = iosRootViewController() ?: run {
        onCancel()
        return
    }
    val picker = UIImagePickerController()
    picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
    val delegate = GalleryPickerDelegate(onBarcode = onBarcode, onEmpty = onEmpty, onCancel = onCancel)
    retainedDelegate = delegate
    picker.delegate = delegate
    root.presentViewController(picker, animated = true, completion = null)
}

private class GalleryPickerDelegate(
    private val onBarcode: (BarcodePayload) -> Unit,
    private val onEmpty: () -> Unit,
    private val onCancel: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        retainedDelegate = null
        val image: UIImage = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            ?: run {
                onEmpty()
                return
            }
        val payload: BarcodePayload? = IosStillBarcodeDecoder.decode(image)
        if (payload == null) {
            onEmpty()
        } else {
            onBarcode(payload)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        retainedDelegate = null
        onCancel()
    }
}
