package com.hnexperts.cosmetics.ui.a11y

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

fun Modifier.screenHeading(): Modifier {
    return semantics { heading() }
}
