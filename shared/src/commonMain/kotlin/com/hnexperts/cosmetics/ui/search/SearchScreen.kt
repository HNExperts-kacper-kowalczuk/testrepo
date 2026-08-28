package com.hnexperts.cosmetics.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hnexperts.cosmetics.ads.AdPlacement
import com.hnexperts.cosmetics.ads.AppScreen
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.resources.Res
import com.hnexperts.cosmetics.resources.scan_working
import com.hnexperts.cosmetics.resources.search_empty
import com.hnexperts.cosmetics.resources.search_ingredients
import com.hnexperts.cosmetics.resources.search_placeholder
import com.hnexperts.cosmetics.resources.search_placeholder_ingredients
import com.hnexperts.cosmetics.resources.search_products
import com.hnexperts.cosmetics.resources.search_title
import com.hnexperts.cosmetics.resources.a11y_open_ingredient
import com.hnexperts.cosmetics.resources.a11y_open_product
import com.hnexperts.cosmetics.ui.a11y.screenHeading
import com.hnexperts.cosmetics.ui.common.BannerAdSlot
import com.hnexperts.cosmetics.ui.common.BusyStatus
import com.hnexperts.cosmetics.ui.common.ChoiceChip
import com.hnexperts.cosmetics.ui.common.ChoiceChipFlow
import com.hnexperts.cosmetics.ui.common.FailureBanner
import com.hnexperts.cosmetics.ui.common.RatingBadge
import com.hnexperts.cosmetics.ui.common.dangerLevelText
import com.hnexperts.cosmetics.ui.layout.AppWidthColumn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onOpenResult: () -> Unit
) {
    val uiState: SearchUiState by viewModel.uiState.collectAsState()
    val products: List<Product> by viewModel.results.collectAsState()
    val ingredients: List<IngredientHit> by viewModel.ingredientResults.collectAsState()
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
        AppWidthColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(vertical = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.search_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.screenHeading()
            )
            FailureBanner(failure = uiState.failure)
            ChoiceChipFlow(
                chips = listOf(
                    ChoiceChip(
                        SearchMode.PRODUCTS,
                        stringResource(Res.string.search_products),
                        uiState.mode == SearchMode.PRODUCTS
                    ),
                    ChoiceChip(
                        SearchMode.INGREDIENTS,
                        stringResource(Res.string.search_ingredients),
                        uiState.mode == SearchMode.INGREDIENTS
                    )
                ),
                onSelect = viewModel::setMode
            )
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                enabled = !uiState.busy,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                label = {
                    Text(
                        text = if (uiState.mode == SearchMode.PRODUCTS) {
                            stringResource(Res.string.search_placeholder)
                        } else {
                            stringResource(Res.string.search_placeholder_ingredients)
                        }
                    )
                },
                singleLine = true
            )
            if (uiState.busy) {
                BusyStatus(message = stringResource(Res.string.scan_working))
            } else if (uiState.mode == SearchMode.PRODUCTS) {
                ProductResults(
                    products = products,
                    busy = uiState.busy,
                    onOpen = viewModel::openProduct,
                    modifier = Modifier.weight(1f)
                )
            } else {
                IngredientResults(
                    hits = ingredients,
                    onOpen = viewModel::openIngredient,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    uiState.selectedIngredient?.let { hit ->
        IngredientSheet(hit = hit, onDismiss = viewModel::dismissIngredient)
    }
}

@Composable
private fun ProductResults(
    products: List<Product>,
    busy: Boolean,
    onOpen: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Text(text = stringResource(Res.string.search_empty), modifier = modifier)
        return
    }
    val openLabel: String = stringResource(Res.string.a11y_open_product)
    LazyColumn(modifier = modifier) {
        items(products, key = { product -> product.id }) { product ->
            val details: String = listOfNotNull(product.brand, product.category)
                .filter { part -> part.isNotBlank() }
                .joinToString(separator = " · ")
            ListItem(
                headlineContent = { Text(product.name) },
                supportingContent = { if (details.isNotEmpty()) Text(details) },
                modifier = Modifier.clickable(
                    enabled = !busy,
                    role = Role.Button,
                    onClickLabel = openLabel
                ) { onOpen(product) }
            )
        }
    }
}

@Composable
private fun IngredientResults(
    hits: List<IngredientHit>,
    onOpen: (IngredientHit) -> Unit,
    modifier: Modifier = Modifier
) {
    if (hits.isEmpty()) {
        Text(text = stringResource(Res.string.search_empty), modifier = modifier)
        return
    }
    val openLabel: String = stringResource(Res.string.a11y_open_ingredient)
    LazyColumn(modifier = modifier) {
        items(hits, key = { hit -> hit.ingredient.id }) { hit ->
            ListItem(
                headlineContent = { Text(hit.ingredient.inciName) },
                supportingContent = {
                    val cas: String = hit.ingredient.casNumbers.orEmpty()
                    if (cas.isNotEmpty()) Text(cas)
                },
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = openLabel
                ) { onOpen(hit) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientSheet(hit: IngredientHit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Text(text = hit.ingredient.inciName, style = MaterialTheme.typography.headlineSmall)
            hit.ingredient.casNumbers?.let { cas ->
                Text(text = cas, style = MaterialTheme.typography.bodyMedium)
            }
            hit.level?.let { level ->
                RatingBadge(
                    level = level,
                    label = dangerLevelText(level),
                    contentDescription = dangerLevelText(level),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            val comment: LocalizedText? = hit.comments.firstOrNull { item ->
                item.locale == systemAppLocale().tag
            } ?: hit.comments.firstOrNull { item -> item.locale == "en" } ?: hit.comments.firstOrNull()
            if (comment != null) {
                Text(text = comment.summary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
