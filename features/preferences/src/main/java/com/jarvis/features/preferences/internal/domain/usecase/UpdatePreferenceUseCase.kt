package com.jarvis.features.preferences.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.preferences.internal.domain.entity.AppPreference
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class UpdatePreferenceUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(preference: AppPreference, newValue: Any) {
        repository.updatePreference(preference, newValue)
    }
}