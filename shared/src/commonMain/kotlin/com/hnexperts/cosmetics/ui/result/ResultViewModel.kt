package com.hnexperts.cosmetics.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.catalog.application.CatalogGateway
import com.hnexperts.cosmetics.catalog.domain.Product
import com.hnexperts.cosmetics.catalog.domain.ProductRepository
import com.hnexperts.cosmetics.catalog.domain.ProductUsage
import com.hnexperts.cosmetics.concurrency.AppDispatchers
import com.hnexperts.cosmetics.evaluation.application.CatalogAlternative
import com.hnexperts.cosmetics.evaluation.application.EvaluateProduct
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.application.FindLocalAlternatives
import com.hnexperts.cosmetics.evaluation.application.ShareResultText
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.platform.sharePlainText
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import com.hnexperts.cosmetics.scanning.application.PendingVerifySession
import com.hnexperts.cosmetics.scanning.application.VerifyRequest
import com.hnexperts.cosmetics.shelf.domain.ShelfItem
import com.hnexperts.cosmetics.shelf.domain.ShelfKeys
import com.hnexperts.cosmetics.shelf.domain.UserShelf
import kotlin.time.Clock
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ResultUiState(
    val assessment: ProductAssessment? = null,
    val commentLocale: AppLocale = AppLocale.ENGLISH,
    val onShelf: Boolean = false,
    val alternatives: List<CatalogAlternative> = emptyList(),
    val failure: AppFailure? = null,
    val navigateToCamera: Boolean = false
)

class ResultViewModel(
    private val session: EvaluationSession,
    private val preferences: PreferencesStore,
    private val commentLocalizer: CommentLocalizer,
    private val pendingVerify: PendingVerifySession,
    private val shelf: UserShelf,
    private val products: ProductRepository,
    private val catalog: CatalogGateway,
    private val evaluateProduct: EvaluateProduct,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    private val state: MutableStateFlow<ResultUiState> = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun commentFor(comments: List<LocalizedText>): LocalizedText? {
        return commentLocalizer.pick(comments, state.value.commentLocale)
    }

    fun checkTheLabel() {
        val assessment: ProductAssessment = state.value.assessment ?: return
        viewModelScope.launch {
            val source: String = session.currentSource()
            pendingVerify.publishVerify(
                VerifyRequest(
                    gtin = assessment.gtin,
                    catalogInci = assessment.inciRaw,
                    productName = assessment.productName,
                    brand = assessment.brand,
                    usage = assessment.usage,
                    source = source
                )
            )
            state.value = state.value.copy(navigateToCamera = true)
        }
    }

    fun toggleShelf() {
        val assessment: ProductAssessment = state.value.assessment ?: return
        viewModelScope.launch {
            val key: String = ShelfKeys.of(assessment)
            if (state.value.onShelf) {
                when (val removed: Outcome<Unit> = shelf.remove(key)) {
                    is Outcome.Err -> state.value = state.value.copy(failure = removed.failure)
                    is Outcome.Ok -> state.value = state.value.copy(onShelf = false, failure = null)
                }
                return@launch
            }
            when (val saved: Outcome<Unit> = shelf.save(toShelfItem(assessment, key))) {
                is Outcome.Err -> state.value = state.value.copy(failure = saved.failure)
                is Outcome.Ok -> state.value = state.value.copy(onShelf = true, failure = null)
            }
        }
    }

    fun share() {
        val assessment: ProductAssessment = state.value.assessment ?: return
        sharePlainText(
            title = assessment.productName ?: assessment.gtin ?: "INCI Scan",
            body = ShareResultText.format(assessment)
        )
    }

    fun openAlternative(alternative: CatalogAlternative) {
        viewModelScope.launch {
            val product: Product = alternative.product
            when (
                val scored: Outcome<ProductAssessment> = evaluateProduct.invoke(
                    inciRaw = product.inciRaw,
                    source = "search",
                    productName = product.name,
                    brand = product.brand,
                    usage = ProductUsage.parse(product.usage),
                    category = product.category,
                    productId = product.id
                )
            ) {
                is Outcome.Err -> state.value = state.value.copy(failure = scored.failure)
                is Outcome.Ok -> state.value = state.value.copy(assessment = scored.value, failure = null)
            }
            loadShelfAndAlternatives(state.value.assessment ?: return@launch, state.value.commentLocale)
        }
    }

    fun consumeNavigation() {
        state.value = state.value.copy(navigateToCamera = false)
    }

    private suspend fun load() {
        coroutineScope {
            val storedDeferred = async { preferences.load() }
            val assessmentDeferred = async { session.currentAssessment() }
            val stored: Outcome<StoredPreferences> = storedDeferred.await()
            val assessment: ProductAssessment? = assessmentDeferred.await()
            when (stored) {
                is Outcome.Err -> state.value = ResultUiState(assessment = assessment, failure = stored.failure)
                is Outcome.Ok -> {
                    val locale: AppLocale = commentLocaleOf(stored.value)
                    state.value = ResultUiState(assessment = assessment, commentLocale = locale, failure = null)
                    if (assessment != null) {
                        loadShelfAndAlternatives(assessment, locale)
                    }
                }
            }
        }
    }

    private suspend fun loadShelfAndAlternatives(assessment: ProductAssessment, locale: AppLocale) {
        val onShelf: Boolean = when (val present: Outcome<Boolean> = shelf.contains(ShelfKeys.of(assessment))) {
            is Outcome.Err -> {
                state.value = state.value.copy(failure = present.failure)
                false
            }
            is Outcome.Ok -> present.value
        }
        val alternatives: List<CatalogAlternative> = loadAlternatives(assessment)
        state.value = state.value.copy(
            onShelf = onShelf,
            alternatives = alternatives,
            commentLocale = locale
        )
    }

    private suspend fun loadAlternatives(assessment: ProductAssessment): List<CatalogAlternative> {
        val category: String = assessment.category?.trim().orEmpty()
        if (category.isEmpty()) {
            return emptyList()
        }
        val candidates: List<Product> = when (val found: Outcome<List<Product>> = products.findByCategory(category, FindLocalAlternatives.CANDIDATE_CAP)) {
            is Outcome.Err -> return emptyList()
            is Outcome.Ok -> found.value
        }
        val index = when (val loaded = catalog.awaitIndex()) {
            is Outcome.Err -> return emptyList()
            is Outcome.Ok -> loaded.value
        }
        val stored: StoredPreferences = when (val prefs: Outcome<StoredPreferences> = preferences.load()) {
            is Outcome.Err -> return emptyList()
            is Outcome.Ok -> prefs.value
        }
        return withContext(dispatchers.computation) {
            FindLocalAlternatives.invoke(
                current = assessment,
                candidates = candidates,
                evaluateFormula = index.evaluateFormula,
                profile = stored.profile
            )
        }
    }

    private fun toShelfItem(assessment: ProductAssessment, key: String): ShelfItem {
        return ShelfItem(
            shelfKey = key,
            productId = assessment.productId,
            gtin = assessment.gtin,
            name = assessment.productName,
            brand = assessment.brand,
            inciRaw = assessment.inciRaw,
            rating = assessment.overall.name,
            usage = assessment.usage,
            savedAt = Clock.System.now().toString()
        )
    }

    private fun commentLocaleOf(stored: StoredPreferences): AppLocale {
        return when (stored.localePreference) {
            LocalePreference.PINNED -> stored.pinnedLocale ?: AppLocale.ENGLISH
            LocalePreference.FOLLOW_SYSTEM -> systemAppLocale()
        }
    }
}
