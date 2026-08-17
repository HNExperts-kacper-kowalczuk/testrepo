package com.hnexperts.cosmetics.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val assessment: ProductAssessment? = null,
    val commentLocale: AppLocale = AppLocale.ENGLISH
)

class ResultViewModel(
    private val session: EvaluationSession,
    private val preferences: SqlPreferencesRepository,
    private val commentLocalizer: CommentLocalizer
) : ViewModel() {
    private val state: MutableStateFlow<ResultUiState> = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            coroutineScope {
                val storedDeferred = async { preferences.load() }
                val assessmentDeferred = async { session.currentAssessment() }
                val stored = storedDeferred.await()
                val locale: AppLocale = when (stored.localePreference) {
                    LocalePreference.PINNED -> stored.pinnedLocale ?: AppLocale.ENGLISH
                    LocalePreference.FOLLOW_SYSTEM -> systemAppLocale()
                }
                state.value = ResultUiState(
                    assessment = assessmentDeferred.await(),
                    commentLocale = locale
                )
            }
        }
    }

    fun commentFor(comments: List<LocalizedText>): LocalizedText? {
        return commentLocalizer.pick(comments, state.value.commentLocale)
    }
}
