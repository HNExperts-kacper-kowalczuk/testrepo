package com.hnexperts.cosmetics.ui.chrome

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Small in-repo vectors so chrome does not depend on material-icons-extended,
 * which often links as empty glyphs on iOS.
 */
object AppActionIcons {
    val Camera: ImageVector = icon("Camera") {
        moveTo(9f, 3f)
        lineTo(7.17f, 5f)
        horizontalLineTo(4f)
        curveTo(2.9f, 5f, 2f, 5.9f, 2f, 7f)
        verticalLineTo(19f)
        curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
        horizontalLineTo(20f)
        curveTo(21.1f, 21f, 22f, 20.1f, 22f, 19f)
        verticalLineTo(7f)
        curveTo(22f, 5.9f, 21.1f, 5f, 20f, 5f)
        horizontalLineTo(16.83f)
        lineTo(15f, 3f)
        horizontalLineTo(9f)
        close()
        moveTo(12f, 18f)
        curveTo(9.24f, 18f, 7f, 15.76f, 7f, 13f)
        reflectiveCurveTo(9.24f, 8f, 12f, 8f)
        reflectiveCurveTo(17f, 10.24f, 17f, 13f)
        reflectiveCurveTo(14.76f, 18f, 12f, 18f)
        close()
        moveTo(12f, 10f)
        curveTo(10.34f, 10f, 9f, 11.34f, 9f, 13f)
        reflectiveCurveTo(10.34f, 16f, 12f, 16f)
        reflectiveCurveTo(15f, 14.66f, 15f, 13f)
        reflectiveCurveTo(13.66f, 10f, 12f, 10f)
        close()
    }

    val FlashOn: ImageVector = icon("FlashOn") {
        moveTo(7f, 2f)
        verticalLineTo(13f)
        horizontalLineTo(10f)
        verticalLineTo(22f)
        lineTo(17f, 10f)
        horizontalLineTo(13f)
        lineTo(17f, 2f)
        close()
    }

    val FlashOff: ImageVector = icon("FlashOff") {
        moveTo(17f, 10f)
        horizontalLineTo(13f)
        lineTo(17f, 2f)
        horizontalLineTo(8.4f)
        lineTo(17f, 10.6f)
        close()
        moveTo(3.27f, 3f)
        lineTo(2f, 4.27f)
        lineTo(7f, 9.27f)
        verticalLineTo(13f)
        horizontalLineTo(10f)
        verticalLineTo(18.73f)
        lineTo(16.73f, 21.46f)
        lineTo(18f, 20.19f)
        close()
    }

    val Gallery: ImageVector = icon("Gallery") {
        moveTo(22f, 16f)
        verticalLineTo(4f)
        curveTo(22f, 2.9f, 21.1f, 2f, 20f, 2f)
        horizontalLineTo(8f)
        curveTo(6.9f, 2f, 6f, 2.9f, 6f, 4f)
        verticalLineTo(16f)
        curveTo(6f, 17.1f, 6.9f, 18f, 8f, 18f)
        horizontalLineTo(20f)
        curveTo(21.1f, 18f, 22f, 17.1f, 22f, 16f)
        close()
        moveTo(11f, 12f)
        lineTo(13.03f, 14.71f)
        lineTo(16f, 11f)
        lineTo(20f, 16f)
        horizontalLineTo(8f)
        lineTo(11f, 12f)
        close()
        moveTo(2f, 6f)
        verticalLineTo(20f)
        curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
        horizontalLineTo(18f)
        verticalLineTo(20f)
        horizontalLineTo(4f)
        verticalLineTo(6f)
        close()
    }

    val Barcode: ImageVector = icon("Barcode") {
        moveTo(3f, 5f)
        horizontalLineTo(5f)
        verticalLineTo(19f)
        horizontalLineTo(3f)
        close()
        moveTo(6f, 5f)
        horizontalLineTo(8f)
        verticalLineTo(19f)
        horizontalLineTo(6f)
        close()
        moveTo(9f, 5f)
        horizontalLineTo(10f)
        verticalLineTo(19f)
        horizontalLineTo(9f)
        close()
        moveTo(11f, 5f)
        horizontalLineTo(14f)
        verticalLineTo(19f)
        horizontalLineTo(11f)
        close()
        moveTo(15f, 5f)
        horizontalLineTo(16f)
        verticalLineTo(19f)
        horizontalLineTo(15f)
        close()
        moveTo(17f, 5f)
        horizontalLineTo(19f)
        verticalLineTo(19f)
        horizontalLineTo(17f)
        close()
        moveTo(20f, 5f)
        horizontalLineTo(21f)
        verticalLineTo(19f)
        horizontalLineTo(20f)
        close()
    }

    val Document: ImageVector = icon("Document") {
        moveTo(14f, 2f)
        horizontalLineTo(6f)
        curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
        verticalLineTo(20f)
        curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
        horizontalLineTo(18f)
        curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
        verticalLineTo(8f)
        close()
        moveTo(13f, 9f)
        verticalLineTo(3.5f)
        lineTo(18.5f, 9f)
        close()
        moveTo(8f, 12f)
        horizontalLineTo(16f)
        verticalLineTo(14f)
        horizontalLineTo(8f)
        close()
        moveTo(8f, 16f)
        horizontalLineTo(16f)
        verticalLineTo(18f)
        horizontalLineTo(8f)
        close()
    }

    val Copy: ImageVector = icon("Copy") {
        moveTo(16f, 1f)
        horizontalLineTo(4f)
        curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
        verticalLineTo(17f)
        horizontalLineTo(4f)
        verticalLineTo(3f)
        horizontalLineTo(16f)
        close()
        moveTo(19f, 5f)
        horizontalLineTo(8f)
        curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
        verticalLineTo(21f)
        curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
        horizontalLineTo(19f)
        curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
        verticalLineTo(7f)
        curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
        close()
        moveTo(19f, 21f)
        horizontalLineTo(8f)
        verticalLineTo(7f)
        horizontalLineTo(19f)
        close()
    }

    val Image: ImageVector = icon("Image") {
        moveTo(21f, 19f)
        verticalLineTo(5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
        horizontalLineTo(5f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        verticalLineTo(19f)
        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
        horizontalLineTo(19f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        close()
        moveTo(8.5f, 13.5f)
        lineTo(11f, 16.51f)
        lineTo(14.5f, 12f)
        lineTo(19f, 18f)
        horizontalLineTo(5f)
        close()
    }

    val Compare: ImageVector = icon("Compare") {
        moveTo(9.01f, 14f)
        horizontalLineTo(2f)
        verticalLineTo(16f)
        horizontalLineTo(9.01f)
        verticalLineTo(19f)
        lineTo(13f, 15f)
        lineTo(9.01f, 11f)
        close()
        moveTo(14.99f, 13f)
        verticalLineTo(10f)
        horizontalLineTo(22f)
        verticalLineTo(8f)
        horizontalLineTo(14.99f)
        verticalLineTo(5f)
        lineTo(11f, 9f)
        close()
    }

    private fun icon(name: String, pathBuilder: PathBuilder.() -> Unit): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black), pathBuilder = pathBuilder)
        }.build()
    }
}
