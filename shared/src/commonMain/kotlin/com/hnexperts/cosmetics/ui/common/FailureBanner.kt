package com.hnexperts.cosmetics.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.common_retry
import com.hnexperts.cosmetics.resources.error_catalog
import com.hnexperts.cosmetics.resources.error_corrupt_catalog
import com.hnexperts.cosmetics.resources.error_database
import com.hnexperts.cosmetics.resources.error_evaluation
import com.hnexperts.cosmetics.resources.error_camera
import com.hnexperts.cosmetics.resources.error_ocr
import com.hnexperts.cosmetics.resources.error_network
import com.hnexperts.cosmetics.resources.error_unexpected
import com.hnexperts.cosmetics.ui.motion.Reveal
import org.jetbrains.compose.resources.stringResource

@Composable
fun FailureBanner(
    failure: AppFailure?,
    onRetry: (() -> Unit)? = null
) {
    Reveal(visible = failure != null) {
        val shown: AppFailure = failure ?: return@Reveal
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Assertive
                }
            ) {
                Text(
                    text = titleFor(shown),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = shown.verboseMessage(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text(stringResource(Res.string.common_retry))
                }
            }
        }
    }
}

@Composable
private fun titleFor(failure: AppFailure): String {
    return when (failure) {
        is AppFailure.CatalogLoad -> stringResource(Res.string.error_catalog)
        is AppFailure.CorruptCatalog -> stringResource(Res.string.error_corrupt_catalog)
        is AppFailure.Database -> stringResource(Res.string.error_database)
        is AppFailure.Evaluation -> stringResource(Res.string.error_evaluation)
        is AppFailure.Camera -> stringResource(Res.string.error_camera)
        is AppFailure.Ocr -> stringResource(Res.string.error_ocr)
        is AppFailure.Network -> stringResource(Res.string.error_network)
        is AppFailure.Unexpected -> stringResource(Res.string.error_unexpected)
    }
}
