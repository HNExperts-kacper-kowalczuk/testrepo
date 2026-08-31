package com.hnexperts.cosmetics.scanning.ios

import com.hnexperts.cosmetics.platform.iosRootViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

private var retainedDelegate: IosGalleryPickerDelegate? = null

internal fun presentIosGalleryPicker(
    onImage: (UIImage) -> Unit,
    onEmpty: () -> Unit,
    onCancel: () -> Unit
) {
    val root = iosRootViewController() ?: run {
        onCancel()
        return
    }
    val picker = UIImagePickerController()
    picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
    val delegate = IosGalleryPickerDelegate(onImage = onImage, onEmpty = onEmpty, onCancel = onCancel)
    retainedDelegate = delegate
    picker.delegate = delegate
    root.presentViewController(picker, animated = true, completion = null)
}

private class IosGalleryPickerDelegate(
    private val onImage: (UIImage) -> Unit,
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
        onImage(image)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        retainedDelegate = null
        onCancel()
    }
}
