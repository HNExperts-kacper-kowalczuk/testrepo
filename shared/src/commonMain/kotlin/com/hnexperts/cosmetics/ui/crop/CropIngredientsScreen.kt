package com.hnexperts.cosmetics.ui.crop

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.crop_hint
import com.hnexperts.cosmetics.resources.crop_reset
import com.hnexperts.cosmetics.resources.crop_retake
import com.hnexperts.cosmetics.resources.crop_title
import com.hnexperts.cosmetics.resources.crop_use
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.scanning.domain.CornerPoint
import com.hnexperts.cosmetics.scanning.domain.QuadCorner
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import com.hnexperts.cosmetics.ui.common.FailureBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropIngredientsScreen(
    viewModel: CropIngredientsViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val uiState: CropUiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.navigateToConfirm) {
        if (uiState.navigateToConfirm) {
            onConfirm()
            viewModel.consumeNavigation()
        }
    }
    LaunchedEffect(uiState.missingCapture) {
        if (uiState.missingCapture) {
            onBack()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.crop_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.abandonCapture()
                        onBack()
                    }) {
                        Text(stringResource(Res.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FailureBanner(failure = uiState.failure)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                CropStage(uiState = uiState, viewModel = viewModel)
            }
            CropControls(uiState = uiState, viewModel = viewModel, onRetake = {
                viewModel.abandonCapture()
                onBack()
            })
        }
    }
}

@Composable
private fun CropStage(uiState: CropUiState, viewModel: CropIngredientsViewModel) {
    val jpeg: ByteArray? = uiState.previewJpeg
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, key1 = jpeg) {
        value = jpeg?.let { bytes ->
            withContext(Dispatchers.Default) { bytes.decodeToImageBitmap() }
        }
    }
    val loaded: ImageBitmap = bitmap ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val handleRadiusPx: Float = with(LocalDensity.current) { HANDLE_RADIUS.toPx() }
    val touchRadiusPx: Float = with(LocalDensity.current) { TOUCH_RADIUS.toPx() }
    val accent: Color = MaterialTheme.colorScheme.primary
    Image(
        bitmap = loaded,
        contentDescription = stringResource(Res.string.crop_hint),
        modifier = Modifier.fillMaxSize()
    )
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(loaded) {
                var active: QuadCorner? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        val rect: Rect = fittedRect(Size(size.width.toFloat(), size.height.toFloat()), loaded)
                        active = nearestCorner(viewModel.uiState.value.quad, rect, offset, touchRadiusPx)
                    },
                    onDragEnd = { active = null },
                    onDragCancel = { active = null }
                ) { change, _ ->
                    val corner: QuadCorner = active ?: return@detectDragGestures
                    change.consume()
                    val rect: Rect = fittedRect(Size(size.width.toFloat(), size.height.toFloat()), loaded)
                    viewModel.updateCorner(corner, toNormalized(change.position, rect))
                }
            }
    ) {
        val rect: Rect = fittedRect(size, loaded)
        drawQuadOverlay(rect, viewModel.uiState.value.quad, accent, handleRadiusPx)
    }
}

@Composable
private fun CropControls(
    uiState: CropUiState,
    viewModel: CropIngredientsViewModel,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(Res.string.crop_hint), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = viewModel::resetQuad, enabled = !uiState.busy) {
                Text(stringResource(Res.string.crop_reset))
            }
            TextButton(onClick = onRetake, enabled = !uiState.busy) {
                Text(stringResource(Res.string.crop_retake))
            }
        }
        Button(
            onClick = viewModel::useSelection,
            enabled = !uiState.busy && uiState.previewJpeg != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.crop_use))
        }
        if (uiState.busy) {
            CircularProgressIndicator()
            Text(text = stringResource(Res.string.scan_working))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQuadOverlay(
    rect: Rect,
    quad: SelectionQuad,
    accent: Color,
    handleRadius: Float
) {
    val points: List<Offset> = listOf(
        toCanvas(quad.topLeft, rect),
        toCanvas(quad.topRight, rect),
        toCanvas(quad.bottomRight, rect),
        toCanvas(quad.bottomLeft, rect)
    )
    val quadPath: Path = Path().apply {
        fillType = PathFillType.EvenOdd
        addRect(Rect(Offset.Zero, size))
        moveTo(points[0].x, points[0].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[2].x, points[2].y)
        lineTo(points[3].x, points[3].y)
        close()
    }
    drawPath(quadPath, color = Color.Black.copy(alpha = 0.5f))
    for (index in points.indices) {
        val next: Offset = points[(index + 1) % points.size]
        drawLine(color = accent, start = points[index], end = next, strokeWidth = 4f)
    }
    for (point in points) {
        drawCircle(color = Color.White, radius = handleRadius, center = point)
        drawCircle(color = accent, radius = handleRadius, center = point, style = Stroke(width = 5f))
    }
}

private fun fittedRect(container: Size, bitmap: ImageBitmap): Rect {
    val imageWidth: Float = bitmap.width.toFloat()
    val imageHeight: Float = bitmap.height.toFloat()
    val scale: Float = minOf(container.width / imageWidth, container.height / imageHeight)
    val width: Float = imageWidth * scale
    val height: Float = imageHeight * scale
    val left: Float = (container.width - width) / 2f
    val top: Float = (container.height - height) / 2f
    return Rect(left, top, left + width, top + height)
}

private fun toCanvas(point: CornerPoint, rect: Rect): Offset {
    return Offset(
        x = rect.left + point.x * rect.width,
        y = rect.top + point.y * rect.height
    )
}

private fun toNormalized(position: Offset, rect: Rect): CornerPoint {
    return CornerPoint(
        x = (position.x - rect.left) / rect.width,
        y = (position.y - rect.top) / rect.height
    )
}

private fun nearestCorner(
    quad: SelectionQuad,
    rect: Rect,
    touch: Offset,
    touchRadius: Float
): QuadCorner? {
    var best: QuadCorner? = null
    var bestDistance: Float = touchRadius
    for ((corner, point) in quad.corners()) {
        val canvasPoint: Offset = toCanvas(point, rect)
        val distance: Float = (canvasPoint - touch).getDistance()
        if (distance <= bestDistance) {
            best = corner
            bestDistance = distance
        }
    }
    return best
}

private val HANDLE_RADIUS = 14.dp
private val TOUCH_RADIUS = 32.dp
