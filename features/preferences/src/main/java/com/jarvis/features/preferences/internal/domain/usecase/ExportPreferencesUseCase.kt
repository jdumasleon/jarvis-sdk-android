package com.jarvis.features.preferences.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.preferences.internal.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ExportPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(storageType: PreferenceStorageType?): String = repository.exportPreferences()
}