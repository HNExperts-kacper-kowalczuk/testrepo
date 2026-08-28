package com.hnexperts.cosmetics.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.evaluation.application.ShareCopy
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.result_share
import com.hnexperts.cosmetics.resources.result_share_image
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultShareActions(
    copy: ShareCopy,
    viewModel: ResultViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { viewModel.share(copy) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.result_share))
        }
        Button(
            onClick = { viewModel.shareImage(copy) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.result_share_image))
        }
    }
}
