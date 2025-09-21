package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import javax.inject.Inject

class ImportPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(data: String, targetStorageType: PreferenceStorageType): Result<Unit> = 
        repository.importPreferences(data, targetStorageType)
}