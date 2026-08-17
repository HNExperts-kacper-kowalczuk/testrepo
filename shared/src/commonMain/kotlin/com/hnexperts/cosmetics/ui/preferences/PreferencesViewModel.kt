package com.hnexperts.cosmetics.ui.preferences

import androidx.lifecycle.ViewModel
import com.hnexperts.cosmetics.catalog.application.CatalogIndex
import com.hnexperts.cosmetics.i18n.AppLocale
import com.hnexperts.cosmetics.i18n.LocalePreference
import com.hnexperts.cosmetics.ingredients.domain.Ingredient
import com.hnexperts.cosmetics.preferences.data.SqlPreferencesRepository
import com.hnexperts.cosmetics.preferences.data.StoredPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesViewModel(
    private val repository: SqlPreferencesRepository,
    private val catalogIndex: CatalogIndex
) : ViewModel() {
    private val state: MutableStateFlow<StoredPreferences> = MutableStateFlow(repository.load())
    val preferences: StateFlow<StoredPreferences> = state.asStateFlow()

    fun ingredients(): List<Ingredient> {
        return catalogIndex.ingredientsById.values.sortedBy { ingredient -> ingredient.inciName }
    }

    fun setPregnancyCaution(enabled: Boolean) {
        update { current ->
            current.copy(profile = current.profile.copy(pregnancyCaution = enabled))
        }
    }

    fun setFragranceFree(enabled: Boolean) {
        update { current ->
            current.copy(profile = current.profile.copy(fragranceFree = enabled))
        }
    }

    fun setFollowSystemLocale() {
        update { current ->
            current.copy(localePreference = LocalePreference.FOLLOW_SYSTEM, pinnedLocale = null)
        }
    }

    fun pinLocale(locale: AppLocale) {
        update { current ->
            current.copy(localePreference = LocalePreference.PINNED, pinnedLocale = locale)
        }
    }

    fun toggleAvoid(ingredientId: String) {
        update { current ->
            val next: Set<String> = if (current.profile.avoidedIngredientIds.contains(ingredientId)) {
                current.profile.avoidedIngredientIds - ingredientId
            } else {
                current.profile.avoidedIngredientIds + ingredientId
            }
            current.copy(profile = current.profile.copy(avoidedIngredientIds = next))
        }
    }

    private fun update(transform: (StoredPreferences) -> StoredPreferences) {
        val next: StoredPreferences = transform(state.value)
        repository.save(next)
        state.value = next
    }
}
