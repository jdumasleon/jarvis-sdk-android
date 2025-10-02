package com.jarvis.demo.koin.domain.usecase.preferences

import com.jarvis.demo.koin.data.preferences.DemoPreferencesRepository
import com.jarvis.demo.koin.presentation.preferences.PreferenceItem
import kotlinx.coroutines.flow.Flow

class GetAllPreferencesUseCase (
    private val repository: DemoPreferencesRepository
) {
    operator fun invoke(): Flow<List<PreferenceItem>> {
        return repository.getAllPreferencesFlow()
    }
}