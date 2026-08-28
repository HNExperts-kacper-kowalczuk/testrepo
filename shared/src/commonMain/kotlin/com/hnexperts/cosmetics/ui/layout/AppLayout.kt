package com.hnexperts.cosmetics.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Readable line length and gutters that grow on tablets.
 * Compact &lt; 600dp, medium 600–839, expanded ≥ 840 (Material window sizes).
 */
object AppLayout {
    const val MAX_READABLE_DP: Int = 600
    const val MIN_TOUCH_DP: Int = 48
    const val COMPACT_GUTTER_DP: Int = 16
    const val MEDIUM_GUTTER_DP: Int = 24
    const val EXPANDED_MIN_GUTTER_DP: Int = 32
    const val MEDIUM_MIN_DP: Int = 600
    const val EXPANDED_MIN_DP: Int = 840

    fun horizontalGutterDp(containerWidthDp: Int): Int {
        if (containerWidthDp >= EXPANDED_MIN_DP) {
            val centering: Int = (containerWidthDp - MAX_READABLE_DP) / 2
            return maxOf(EXPANDED_MIN_GUTTER_DP, centering)
        }
        if (containerWidthDp >= MEDIUM_MIN_DP) {
            return MEDIUM_GUTTER_DP
        }
        return COMPACT_GUTTER_DP
    }

    fun contentWidthDp(containerWidthDp: Int): Int {
        val gutter: Int = horizontalGutterDp(containerWidthDp)
        val inner: Int = containerWidthDp - (2 * gutter)
        return inner.coerceAtLeast(0)
    }
}

val AppLayout.minTouchTarget: Dp
    get() = AppLayout.MIN_TOUCH_DP.dp

@Composable
fun AppScrollPane(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gutter: Dp = AppLayout.horizontalGutterDp(maxWidth.value.toInt()).dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = gutter, vertical = 16.dp),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

@Composable
fun AppWidthColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gutter: Dp = AppLayout.horizontalGutterDp(maxWidth.value.toInt()).dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .imePadding()
                .padding(horizontal = gutter),
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}
