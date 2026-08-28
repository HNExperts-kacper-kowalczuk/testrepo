package com.hnexperts.cosmetics.ui.crop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.back
import com.hnexperts.cosmetics.resources.crop_handle_bottom_left
import com.hnexperts.cosmetics.resources.crop_handle_bottom_right
import com.hnexperts.cosmetics.resources.crop_handle_top_left
import com.hnexperts.cosmetics.resources.crop_handle_top_right
import com.hnexperts.cosmetics.resources.crop_hint
import com.hnexperts.cosmetics.resources.crop_reset
import com.hnexperts.cosmetics.resources.crop_retake
import com.hnexperts.cosmetics.resources.crop_title
import com.hnexperts.cosmetics.resources.crop_use
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.scanning.domain.QuadCorner
import com.hnexperts.cosmetics.scanning.domain.SelectionQuad
import com.hnexperts.cosmetics.ui.common.BusyStatus
import com.hnexperts.cosmetics.ui.common.FailureBanner
import kotlin.math.roundToInt
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
    val loaded: ImageBitmap? = bitmap
    if (loaded == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val accent: Color = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val rect: FittedImageRect = CropQuadGeometry.fittedRect(
            containerWidth = with(density) { maxWidth.toPx() },
            containerHeight = with(density) { maxHeight.toPx() },
            imageWidth = loaded.width,
            imageHeight = loaded.height
        )
        Image(
            bitmap = loaded,
            contentDescription = stringResource(Res.string.crop_hint),
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        )
        QuadOverlay(rect = rect, quad = uiState.quad, accent = accent)
        for ((corner, point) in uiState.quad.corners()) {
            val (cx, cy) = CropQuadGeometry.toCanvas(point.x, point.y, rect)
            CropHandle(
                centerX = cx,
                centerY = cy,
                accent = accent,
                enabled = !uiState.busy,
                label = handleLabel(corner),
                onDrag = { dx, dy -> viewModel.nudgeCorner(corner, dx, dy, rect.width, rect.height) }
            )
        }
    }
}

@Composable
private fun QuadOverlay(rect: FittedImageRect, quad: SelectionQuad, accent: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val points: List<Offset> = listOf(
            Offset(rect.left + quad.topLeft.x * rect.width, rect.top + quad.topLeft.y * rect.height),
            Offset(rect.left + quad.topRight.x * rect.width, rect.top + quad.topRight.y * rect.height),
            Offset(rect.left + quad.bottomRight.x * rect.width, rect.top + quad.bottomRight.y * rect.height),
            Offset(rect.left + quad.bottomLeft.x * rect.width, rect.top + quad.bottomLeft.y * rect.height)
        )
        val dim: Path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            moveTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
            lineTo(points[3].x, points[3].y)
            close()
        }
        drawPath(dim, color = Color.Black.copy(alpha = 0.45f))
        for (index in points.indices) {
            drawLine(color = accent, start = points[index], end = points[(index + 1) % points.size], strokeWidth = 5f)
        }
    }
}

@Composable
private fun BoxScope.CropHandle(
    centerX: Float,
    centerY: Float,
    accent: Color,
    enabled: Boolean,
    label: String,
    onDrag: (Float, Float) -> Unit
) {
    val handlePx: Float = with(LocalDensity.current) { HANDLE_SIZE.toPx() }
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset {
                IntOffset(
                    x = (centerX - handlePx / 2f).roundToInt(),
                    y = (centerY - handlePx / 2f).roundToInt()
                )
            }
            .size(HANDLE_SIZE)
            .semantics { contentDescription = label }
            .pointerInput(enabled) {
                if (!enabled) {
                    return@pointerInput
                }
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color.White, CircleShape)
                .border(width = 3.dp, color = accent, shape = CircleShape)
        )
    }
}

@Composable
private fun handleLabel(corner: QuadCorner): String {
    return when (corner) {
        QuadCorner.TOP_LEFT -> stringResource(Res.string.crop_handle_top_left)
        QuadCorner.TOP_RIGHT -> stringResource(Res.string.crop_handle_top_right)
        QuadCorner.BOTTOM_RIGHT -> stringResource(Res.string.crop_handle_bottom_right)
        QuadCorner.BOTTOM_LEFT -> stringResource(Res.string.crop_handle_bottom_left)
    }
}

@Composable
private fun CropControls(
    uiState: CropUiState,
    viewModel: CropIngredientsViewModel,
    onRetake: () -> Unit
) {
    var selectedCorner: QuadCorner by remember { mutableStateOf(QuadCorner.TOP_LEFT) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(Res.string.crop_hint), style = MaterialTheme.typography.bodySmall)
        CropNudgeBar(
            selected = selectedCorner,
            enabled = !uiState.busy,
            onSelect = { corner -> selectedCorner = corner },
            onNudge = { dx, dy -> viewModel.nudgeNormalized(selectedCorner, dx, dy) }
        )
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
            BusyStatus(message = stringResource(Res.string.scan_working))
        }
    }
}

private val HANDLE_SIZE = 48.dp
