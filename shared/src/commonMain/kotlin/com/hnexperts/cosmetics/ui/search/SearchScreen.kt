package com.hnexperts.cosmetics.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPolicy
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.search_empty
import com.hnexperts.cosmetics.resources.search_placeholder
import com.hnexperts.cosmetics.resources.search_title
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenResult: () -> Unit
) {
    var query: String by remember { mutableStateOf("") }
    val products: List<Product> = remember(query) { viewModel.query(query) }
    Scaffold(
        bottomBar = {
            BannerAdSlot(
                visible = AdPolicy().shouldShowBanner(
                    AppScreen.SEARCH,
                    consentGranted = true,
                    networkAvailable = false
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = stringResource(Res.string.search_title), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = query,
                onValueChange = { value -> query = value },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                label = { Text(stringResource(Res.string.search_placeholder)) },
                singleLine = true
            )
            if (products.isEmpty()) {
                Text(text = stringResource(Res.string.search_empty))
            } else {
                LazyColumn {
                    items(products, key = { product -> product.id }) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text(product.brand.orEmpty()) },
                            modifier = Modifier.clickable {
                                viewModel.openProduct(product)
                                onOpenResult()
                            }
                        )
                    }
                }
            }
        }
    }
}
