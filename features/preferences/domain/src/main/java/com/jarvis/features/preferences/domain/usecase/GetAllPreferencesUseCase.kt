package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): Flow<List<AppPreference>> = repository.getAllPreferences()
}