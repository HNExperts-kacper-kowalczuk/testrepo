package com.hnexperts.cosmetics.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.failure.AppFailure
import com.hnexperts.cosmetics.failure.Outcome
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.preferences.domain.PreferencesStore
import com.hnexperts.cosmetics.preferences.domain.StoredPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val assessment: ProductAssessment? = null,
    val commentLocale: AppLocale = AppLocale.ENGLISH,
    val failure: AppFailure? = null
)

class ResultViewModel(
    private val session: EvaluationSession,
    private val preferences: PreferencesStore,
    private val commentLocalizer: CommentLocalizer
) : ViewModel() {
    private val state: MutableStateFlow<ResultUiState> = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            coroutineScope {
                val storedDeferred = async { preferences.load() }
                val assessmentDeferred = async { session.currentAssessment() }
                val stored: Outcome<StoredPreferences> = storedDeferred.await()
                val assessment: ProductAssessment? = assessmentDeferred.await()
                when (stored) {
                    is Outcome.Err -> state.value = ResultUiState(
                        assessment = assessment,
                        failure = stored.failure
                    )
                    is Outcome.Ok -> {
                        val locale: AppLocale = commentLocaleOf(stored.value)
                        state.value = ResultUiState(
                            assessment = assessment,
                            commentLocale = locale,
                            failure = null
                        )
                    }
                }
            }
        }
    }

    fun commentFor(comments: List<LocalizedText>): LocalizedText? {
        return commentLocalizer.pick(comments, state.value.commentLocale)
    }

    private fun commentLocaleOf(stored: StoredPreferences): AppLocale {
        return when (stored.localePreference) {
            LocalePreference.PINNED -> stored.pinnedLocale ?: AppLocale.ENGLISH
            LocalePreference.FOLLOW_SYSTEM -> systemAppLocale()
        }
    }
}
