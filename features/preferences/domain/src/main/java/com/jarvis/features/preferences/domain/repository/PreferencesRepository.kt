package com.jarvis.features.preferences.domain.repository

import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    
    fun getAllPreferences(): Flow<List<AppPreference>>
    
    fun getPreferencesByStorageType(storageType: PreferenceStorageType): Flow<PreferenceGroup>
    
    fun getFilteredPreferences(filter: PreferenceFilter): Flow<List<AppPreference>>
    
    suspend fun updatePreference(preference: AppPreference, newValue: Any)
    
    suspend fun deletePreference(preference: AppPreference)
    
    suspend fun clearPreferences(storageType: PreferenceStorageType)
    
    suspend fun clearAllPreferences()
    
    suspend fun exportPreferences(storageType: PreferenceStorageType? = null): String
    
    suspend fun importPreferences(data: String, targetStorageType: PreferenceStorageType): Result<Unit>
    
    suspend fun addPreference(key: String, value: Any, type: PreferenceType, storageType: PreferenceStorageType)
}