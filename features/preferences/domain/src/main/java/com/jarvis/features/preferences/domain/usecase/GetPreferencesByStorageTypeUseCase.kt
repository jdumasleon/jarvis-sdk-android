package com.jarvis.features.preferences.domain.usecase

import com.jarvis.features.preferences.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPreferencesByStorageTypeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(storageType: PreferenceStorageType): Flow<PreferenceGroup> = 
        repository.getPreferencesByStorageType(storageType)
}