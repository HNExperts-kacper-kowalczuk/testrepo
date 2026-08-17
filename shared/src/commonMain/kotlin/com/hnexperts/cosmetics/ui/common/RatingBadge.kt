package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.ui.theme.RatingColors

@Composable
fun RatingBadge(
    level: DangerLevel,
    label: String,
    contentDescription: String,
    large: Boolean = false,
    modifier: Modifier = Modifier
) {
    val markSize: Dp = if (large) 28.dp else 14.dp
    Row(
        modifier = modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RatingMark(level = level, size = markSize)
        Text(
            text = label,
            style = if (large) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun RatingMark(level: DangerLevel, size: Dp) {
    val color = RatingColors.of(level)
    val markModifier: Modifier = Modifier.size(size)
    when (level) {
        DangerLevel.SAFE -> Box(
            modifier = markModifier.clip(CircleShape).background(color)
        )
        DangerLevel.LOW -> Box(
            modifier = markModifier
                .clip(CircleShape)
                .border(width = 3.dp, color = color, shape = CircleShape)
        )
        DangerLevel.MODERATE -> Box(
            modifier = markModifier.clip(RoundedCornerShape(2.dp)).background(color)
        )
        DangerLevel.RESTRICTED -> Box(
            modifier = markModifier.rotate(45f).clip(RoundedCornerShape(2.dp)).background(color)
        )
        DangerLevel.HIGH -> Box(
            modifier = markModifier.clip(RoundedCornerShape(topStart = size, topEnd = size, bottomStart = 2.dp, bottomEnd = 2.dp)).background(color)
        )
        DangerLevel.PROHIBITED -> Box(
            modifier = markModifier.clip(RoundedCornerShape(2.dp)).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "X", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
        }
        DangerLevel.UNKNOWN -> Box(
            modifier = markModifier.clip(CircleShape).border(width = 2.dp, color = color, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", color = color, style = MaterialTheme.typography.labelSmall)
        }
    }
}
