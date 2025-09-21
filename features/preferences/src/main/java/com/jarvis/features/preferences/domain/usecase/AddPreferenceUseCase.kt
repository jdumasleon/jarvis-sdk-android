package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import javax.inject.Inject

class AddPreferenceUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(
        key: String, 
        value: Any, 
        type: PreferenceType, 
        storageType: PreferenceStorageType
    ) {
        repository.addPreference(key, value, type, storageType)
    }
}