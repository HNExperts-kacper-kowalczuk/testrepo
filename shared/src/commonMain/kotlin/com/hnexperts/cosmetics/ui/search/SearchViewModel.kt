package com.hnexperts.cosmetics.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.data.SqlProductRepository
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val busy: Boolean = false,
    val navigateToResult: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val products: SqlProductRepository,
    private val evaluateProduct: EvaluateProduct
) : ViewModel() {
    private val queryText: MutableStateFlow<String> = MutableStateFlow("")
    private val navigation: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = navigation.asStateFlow()
    val results: StateFlow<List<Product>> = queryText
        .debounce(250)
        .distinctUntilChanged()
        .mapLatest { text -> products.search(text) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private var openJob: Job? = null

    fun onQueryChange(text: String) {
        queryText.value = text
        navigation.update { current -> current.copy(query = text) }
    }

    fun openProduct(product: Product) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            navigation.update { current -> current.copy(busy = true) }
            try {
                evaluateProduct.invoke(
                    inciRaw = product.inciRaw,
                    source = "search",
                    productName = product.name,
                    brand = product.brand
                )
                navigation.update { current -> current.copy(navigateToResult = true) }
            } finally {
                navigation.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun consumeNavigation() {
        navigation.update { current -> current.copy(navigateToResult = false) }
    }
}
