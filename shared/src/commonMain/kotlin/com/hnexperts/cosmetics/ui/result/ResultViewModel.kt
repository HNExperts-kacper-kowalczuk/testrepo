package com.hnexperts.cosmetics.ui.result

import androidx.lifecycle.ViewModel
import com.hnexperts.cosmetics.evaluation.application.EvaluationSession
import com.hnexperts.cosmetics.evaluation.domain.ProductAssessment
import com.hnexperts.cosmetics.hazards.domain.LocalizedText
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.CommentLocalizer
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.i18n.systemAppLocale
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository

class ResultViewModel(
    private val session: EvaluationSession,
    private val preferences: SqlPreferencesRepository,
    private val commentLocalizer: CommentLocalizer
) : ViewModel() {
    fun assessment(): ProductAssessment? {
        return session.lastAssessment
    }

    fun commentFor(comments: List<LocalizedText>): LocalizedText? {
        val stored = preferences.load()
        val locale: AppLocale = when (stored.localePreference) {
            LocalePreference.PINNED -> stored.pinnedLocale ?: AppLocale.ENGLISH
            LocalePreference.FOLLOW_SYSTEM -> systemAppLocale()
        }
        return commentLocalizer.pick(comments, locale)
    }
}
