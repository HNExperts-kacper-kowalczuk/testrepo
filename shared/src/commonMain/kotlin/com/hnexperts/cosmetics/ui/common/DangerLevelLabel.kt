package com.hnexperts.cosmetics.ui.common

import androidx.compose.runtime.Composable
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.level_high
import com.hnexperts.cosmetics.resources.level_low
import com.hnexperts.cosmetics.resources.level_moderate
import com.hnexperts.cosmetics.resources.level_prohibited
import com.hnexperts.cosmetics.resources.level_restricted
import com.hnexperts.cosmetics.resources.level_safe
import com.hnexperts.cosmetics.resources.level_unknown
import org.jetbrains.compose.resources.StringResource

fun dangerLevelLabel(level: DangerLevel): StringResource {
    return when (level) {
        DangerLevel.SAFE -> Res.string.level_safe
        DangerLevel.LOW -> Res.string.level_low
        DangerLevel.MODERATE -> Res.string.level_moderate
        DangerLevel.RESTRICTED -> Res.string.level_restricted
        DangerLevel.HIGH -> Res.string.level_high
        DangerLevel.PROHIBITED -> Res.string.level_prohibited
        DangerLevel.UNKNOWN -> Res.string.level_unknown
    }
}

@Composable
fun dangerLevelText(level: DangerLevel): String {
    return org.jetbrains.compose.resources.stringResource(dangerLevelLabel(level))
}
