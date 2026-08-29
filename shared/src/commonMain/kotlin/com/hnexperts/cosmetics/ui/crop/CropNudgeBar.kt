package com.hnexperts.cosmetics.ui.crop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.crop_handle_bottom_left
import com.hnexperts.cosmetics.resources.crop_handle_bottom_right
import com.hnexperts.cosmetics.resources.crop_handle_top_left
import com.hnexperts.cosmetics.resources.crop_handle_top_right
import com.hnexperts.cosmetics.resources.crop_nudge_down
import com.hnexperts.cosmetics.resources.crop_nudge_left
import com.hnexperts.cosmetics.resources.crop_nudge_right
import com.hnexperts.cosmetics.resources.crop_nudge_title
import com.hnexperts.cosmetics.resources.crop_nudge_up
import com.hnexperts.cosmetics.scanning.domain.QuadCorner
import com.hnexperts.cosmetics.ui.common.ChoiceChip
import com.hnexperts.cosmetics.ui.common.ChoiceChipFlow
import com.hnexperts.cosmetics.ui.chrome.AppIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun CropNudgeBar(
    selected: QuadCorner,
    enabled: Boolean,
    onSelect: (QuadCorner) -> Unit,
    onNudge: (Float, Float) -> Unit
) {
    val chips: List<ChoiceChip<QuadCorner>> = listOf(
        ChoiceChip(QuadCorner.TOP_LEFT, stringResource(Res.string.crop_handle_top_left), selected == QuadCorner.TOP_LEFT),
        ChoiceChip(QuadCorner.TOP_RIGHT, stringResource(Res.string.crop_handle_top_right), selected == QuadCorner.TOP_RIGHT),
        ChoiceChip(QuadCorner.BOTTOM_LEFT, stringResource(Res.string.crop_handle_bottom_left), selected == QuadCorner.BOTTOM_LEFT),
        ChoiceChip(QuadCorner.BOTTOM_RIGHT, stringResource(Res.string.crop_handle_bottom_right), selected == QuadCorner.BOTTOM_RIGHT)
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(Res.string.crop_nudge_title), style = MaterialTheme.typography.titleSmall)
        ChoiceChipFlow(
            chips = chips,
            onSelect = { corner -> if (enabled) onSelect(corner) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NudgeButton(stringResource(Res.string.crop_nudge_left), Icons.AutoMirrored.Filled.KeyboardArrowLeft, enabled) {
                onNudge(-CropQuadGeometry.NUDGE_STEP, 0f)
            }
            NudgeButton(stringResource(Res.string.crop_nudge_up), Icons.Filled.KeyboardArrowUp, enabled) {
                onNudge(0f, -CropQuadGeometry.NUDGE_STEP)
            }
            NudgeButton(stringResource(Res.string.crop_nudge_down), Icons.Filled.KeyboardArrowDown, enabled) {
                onNudge(0f, CropQuadGeometry.NUDGE_STEP)
            }
            NudgeButton(stringResource(Res.string.crop_nudge_right), Icons.AutoMirrored.Filled.KeyboardArrowRight, enabled) {
                onNudge(CropQuadGeometry.NUDGE_STEP, 0f)
            }
        }
    }
}

@Composable
private fun NudgeButton(label: String, icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    AppIconButton(
        imageVector = icon,
        contentDescription = label,
        onClick = onClick,
        enabled = enabled
    )
}
