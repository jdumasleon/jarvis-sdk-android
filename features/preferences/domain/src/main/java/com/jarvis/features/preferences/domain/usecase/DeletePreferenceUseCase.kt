package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import javax.inject.Inject

class DeletePreferenceUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(preference: AppPreference) {
        repository.deletePreference(preference)
    }
}