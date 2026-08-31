package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.evaluation.application.ShareCopy
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.result_share
import com.hnexperts.cosmetics.resources.result_share_image
import com.hnexperts.cosmetics.ui.chrome.AppActionIcons
import com.hnexperts.cosmetics.ui.chrome.AppIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultShareActions(
    copy: ShareCopy,
    viewModel: ResultViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start)
    ) {
        AppIconButton(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(Res.string.result_share),
            onClick = { viewModel.share(copy) }
        )
        AppIconButton(
            imageVector = AppActionIcons.Image,
            contentDescription = stringResource(Res.string.result_share_image),
            onClick = { viewModel.shareImage(copy) }
        )
    }
}
