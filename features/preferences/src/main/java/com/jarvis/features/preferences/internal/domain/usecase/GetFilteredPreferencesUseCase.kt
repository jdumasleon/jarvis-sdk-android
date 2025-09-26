package com.jarvis.features.preferences.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.preferences.internal.domain.entity.AppPreference
import com.jarvis.features.preferences.internal.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GetFilteredPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(filter: PreferenceFilter): Flow<List<AppPreference>> = 
        repository.getFilteredPreferences(filter)
}