package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import javax.inject.Inject

class ExportPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(storageType: PreferenceStorageType?): String = repository.exportPreferences()
}