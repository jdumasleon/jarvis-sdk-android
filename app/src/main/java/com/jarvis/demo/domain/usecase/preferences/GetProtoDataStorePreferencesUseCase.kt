package com.jarvis.demo.domain.usecase.preferences

import com.jarvis.demo.data.preferences.DemoPreferencesRepository
import com.jarvis.demo.presentation.preferences.PreferenceItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProtoDataStorePreferencesUseCase @Inject constructor(
    private val repository: DemoPreferencesRepository
) {
    operator fun invoke(): Flow<List<PreferenceItem>> {
        return repository.getProtoDataStorePreferencesFlow()
    }
}