package com.jarvis.features.preferences.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.preferences.internal.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.internal.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GetPreferencesByStorageTypeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke(storageType: PreferenceStorageType): Flow<PreferenceGroup> = 
        repository.getPreferencesByStorageType(storageType)
}