package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilteredPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(filter: PreferenceFilter): Flow<List<AppPreference>> = 
        repository.getFilteredPreferences(filter)
}