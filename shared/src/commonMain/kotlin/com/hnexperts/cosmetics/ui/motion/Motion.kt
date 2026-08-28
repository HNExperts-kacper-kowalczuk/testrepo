package com.hnexperts.cosmetics.ui.motion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object Motion {
    const val SHORT_MS: Int = 180
    const val MEDIUM_MS: Int = 280

    fun millis(reduceMotion: Boolean, durationMs: Int): Int {
        return if (reduceMotion) 0 else durationMs
    }
}

@Composable
fun Reveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduceMotion: Boolean = rememberReduceMotion()
    if (reduceMotion) {
        if (visible) {
            content()
        }
        return
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        content()
    }
}
