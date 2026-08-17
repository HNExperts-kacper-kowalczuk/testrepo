package com.hnexperts.cosmetics.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.resources.search_empty
import com.hnexperts.cosmetics.resources.search_placeholder
import com.hnexperts.cosmetics.resources.search_title
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.FailureBanner
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenResult: () -> Unit
) {
    val uiState: SearchUiState by viewModel.uiState.collectAsState()
    val products: List<Product> by viewModel.results.collectAsState()
    LaunchedEffect(uiState.navigateToResult) {
        if (uiState.navigateToResult) {
            onOpenResult()
            viewModel.consumeNavigation()
        }
    }
    Scaffold(
        bottomBar = {
            BannerAdSlot(screen = AppScreen.SEARCH, placement = AdPlacement.SEARCH)
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = stringResource(Res.string.search_title), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            FailureBanner(failure = uiState.failure)
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                enabled = !uiState.busy,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                label = { Text(stringResource(Res.string.search_placeholder)) },
                singleLine = true
            )
            if (uiState.busy) {
                CircularProgressIndicator()
                Text(text = stringResource(Res.string.scan_working))
            } else if (products.isEmpty()) {
                Text(text = stringResource(Res.string.search_empty))
            } else {
                LazyColumn {
                    items(products, key = { product -> product.id }) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text(product.brand.orEmpty()) },
                            modifier = Modifier.clickable(enabled = !uiState.busy) {
                                viewModel.openProduct(product)
                            }
                        )
                    }
                }
            }
        }
    }
}
