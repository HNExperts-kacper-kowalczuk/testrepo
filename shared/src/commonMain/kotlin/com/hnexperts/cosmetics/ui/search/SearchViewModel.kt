package com.hnexperts.cosmetics.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.hazards.domain.DangerLevel
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.ui.ingredient.IngredientDetail
import com.hnexperts.cosmetics.ui.ingredient.IngredientDetailAssembler
import com.hnexperts.cosmetics.ui.runUiAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SearchMode {
    PRODUCTS,
    INGREDIENTS
}

data class IngredientHit(
    val ingredient: Ingredient,
    val level: DangerLevel?,
    val comments: List<LocalizedText>
)

data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.PRODUCTS,
    val busy: Boolean = false,
    val failure: AppFailure? = null,
    val navigateToResult: Boolean = false,
    val selectedDetail: IngredientDetail? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val products: ProductRepository,
    private val evaluateProduct: EvaluateProduct,
    private val catalog: CatalogGateway,
    private val preferences: PreferencesStore,
    private val commentLocalizer: CommentLocalizer
) : ViewModel() {
    private val queryText: MutableStateFlow<String> = MutableStateFlow("")
    private val searchMode: MutableStateFlow<SearchMode> = MutableStateFlow(SearchMode.PRODUCTS)
    private val navigation: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = navigation.asStateFlow()
    private val queryDebounced: Flow<String> = queryText.debounce(250).distinctUntilChanged()
    val results: StateFlow<List<Product>> = combine(queryDebounced, searchMode) { text, mode ->
        text to mode
    }
        .mapLatest { (text, mode) -> searchProducts(text, mode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val ingredientResults: StateFlow<List<IngredientHit>> = combine(queryDebounced, searchMode) { text, mode ->
        text to mode
    }
        .mapLatest { (text, mode) -> searchIngredients(text, mode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private var openJob: Job? = null

    fun onQueryChange(text: String) {
        queryText.value = text
        navigation.update { current -> current.copy(query = text, failure = null) }
    }

    fun setMode(mode: SearchMode) {
        searchMode.value = mode
        navigation.update { current -> current.copy(mode = mode, selectedDetail = null) }
    }

    fun openProduct(product: Product) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            navigation.update { current -> current.copy(busy = true, failure = null) }
            try {
                val assessment = runUiAction(onFailure = ::showFailure) {
                    evaluateProduct.invoke(
                        inciRaw = product.inciRaw,
                        source = "search",
                        productName = product.name,
                        brand = product.brand,
                        usage = ProductUsage.parse(product.usage),
                        category = product.category,
                        productId = product.id
                    )
                }
                if (assessment != null) {
                    navigation.update { current -> current.copy(navigateToResult = true) }
                }
            } finally {
                navigation.update { current -> current.copy(busy = false) }
            }
        }
    }

    fun openIngredient(hit: IngredientHit) {
        openJob?.cancel()
        openJob = viewModelScope.launch {
            showIngredientDetail(hit)
        }
    }

    fun dismissIngredient() {
        navigation.update { current -> current.copy(selectedDetail = null) }
    }

    private suspend fun showIngredientDetail(hit: IngredientHit) {
        val index: CatalogIndex = when (val loaded: Outcome<CatalogIndex> = catalog.awaitIndex()) {
            is Outcome.Err -> {
                showFailure(loaded.failure)
                navigation.update { current -> current.copy(selectedDetail = null) }
                return
            }
            is Outcome.Ok -> loaded.value
        }
        val comment: LocalizedText? = commentLocalizer.pick(hit.comments, commentLocale())
        navigation.update { current ->
            current.copy(
                selectedDetail = IngredientDetailAssembler.fromCatalogIngredient(
                    ingredient = hit.ingredient,
                    index = index,
                    comment = comment,
                    level = hit.level
                ),
                failure = null
            )
        }
    }

    private suspend fun commentLocale(): AppLocale {
        return when (val stored: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Err -> systemAppLocale()
            is Outcome.Ok -> commentLocaleOf(stored.value)
        }
    }

    private fun commentLocaleOf(stored: StoredPreferences): AppLocale {
        return when (stored.localePreference) {
            LocalePreference.PINNED -> stored.pinnedLocale ?: AppLocale.ENGLISH
            LocalePreference.FOLLOW_SYSTEM -> systemAppLocale()
        }
    }

    fun consumeNavigation() {
        navigation.update { current -> current.copy(navigateToResult = false) }
    }

    private suspend fun searchProducts(text: String, mode: SearchMode): List<Product> {
        if (mode != SearchMode.PRODUCTS) {
            return emptyList()
        }
        return when (val result: Outcome<List<Product>> = products.search(text)) {
            is Outcome.Ok -> {
                navigation.update { current -> current.copy(failure = null) }
                result.value
            }
            is Outcome.Err -> {
                showFailure(result.failure)
                emptyList()
            }
        }
    }

    private suspend fun searchIngredients(text: String, mode: SearchMode): List<IngredientHit> {
        if (mode != SearchMode.INGREDIENTS || text.isBlank()) {
            return emptyList()
        }
        val index: CatalogIndex = when (val loaded: Outcome<CatalogIndex> = catalog.awaitIndex()) {
            is Outcome.Err -> {
                showFailure(loaded.failure)
                return emptyList()
            }
            is Outcome.Ok -> loaded.value
        }
        return index.searchIngredients(text).map { ingredient ->
            IngredientHit(
                ingredient = ingredient,
                level = index.hazardsById[ingredient.id]?.dangerLevel,
                comments = index.commentsById[ingredient.id].orEmpty()
            )
        }
    }

    private fun showFailure(failure: AppFailure) {
        navigation.update { current -> current.copy(failure = failure, navigateToResult = false) }
    }
}
