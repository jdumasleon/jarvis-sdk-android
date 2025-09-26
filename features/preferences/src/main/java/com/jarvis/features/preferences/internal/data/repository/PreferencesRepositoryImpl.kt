@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.preferences.internal.data.repository

import androidx.annotation.RestrictTo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jarvis.features.preferences.internal.data.local.PreferencesDataStoreScanner
import com.jarvis.features.preferences.internal.data.local.ProtoDataStoreScanner
import com.jarvis.features.preferences.internal.data.local.SharedPreferencesScanner
import com.jarvis.features.preferences.internal.domain.entity.AppPreference
import com.jarvis.features.preferences.internal.domain.entity.PreferenceFilter
import com.jarvis.features.preferences.internal.domain.entity.PreferenceGroup
import com.jarvis.features.preferences.internal.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.internal.domain.entity.PreferenceType
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferencesScanner: SharedPreferencesScanner,
    private val preferencesDataStoreScanner: PreferencesDataStoreScanner,
    private val protoDataStoreScanner: ProtoDataStoreScanner,
    private val gson: Gson
) : PreferencesRepository {
    
    override fun getAllPreferences(): Flow<List<AppPreference>> {
        return combine(
            flow { 
                val sharedPrefs = sharedPreferencesScanner.scanAllSharedPreferences()
                android.util.Log.d("PreferencesRepository", "SharedPreferences scanner found ${sharedPrefs.size} items")
                emit(sharedPrefs) 
            },
            flow { 
                val dataStorePrefs = preferencesDataStoreScanner.scanAllPreferencesDataStores()
                android.util.Log.d("PreferencesRepository", "PreferencesDataStore scanner found ${dataStorePrefs.size} items")
                emit(dataStorePrefs) 
            },
            flow { 
                val protoPrefs = protoDataStoreScanner.scanAllProtoDataStores()
                android.util.Log.d("PreferencesRepository", "ProtoDataStore scanner found ${protoPrefs.size} items")
                emit(protoPrefs) 
            }
        ) { sharedPrefs, dataStorePrefs, protoPrefs ->
            val combined = (sharedPrefs + dataStorePrefs + protoPrefs)
                .distinctBy { "${it.storageType.name}:${it.key}" }
                .sortedBy { it.key }
            
            android.util.Log.d("PreferencesRepository", """
                Combined preferences: ${combined.size} total
                - SharedPreferences: ${sharedPrefs.size}
                - PreferencesDataStore: ${dataStorePrefs.size}
                - ProtoDataStore: ${protoPrefs.size}
            """.trimIndent())
            
            combined
        }
    }
    
    override fun getPreferencesByStorageType(storageType: PreferenceStorageType): Flow<PreferenceGroup> {
        return when (storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> {
                flow { 
                    emit(PreferenceGroup(
                        storageType = storageType,
                        isLoading = true
                    ))
                    try {
                        val preferences = sharedPreferencesScanner.scanAllSharedPreferences()
                        android.util.Log.d("PreferencesRepository", "SharedPreferences scanner returned ${preferences.size} preferences: ${preferences.map { it.key }}")
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = preferences,
                            isLoading = false
                        ))
                    } catch (e: Exception) {
                        android.util.Log.e("PreferencesRepository", "SharedPreferences scanning failed", e)
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = emptyList(),
                            isLoading = false,
                            error = e.message
                        ))
                    }
                }
            }
            PreferenceStorageType.PREFERENCES_DATASTORE -> {
                flow {
                    emit(PreferenceGroup(
                        storageType = storageType,
                        isLoading = true
                    ))
                    try {
                        val preferences = preferencesDataStoreScanner.scanAllPreferencesDataStores()
                        android.util.Log.d("PreferencesRepository", "PreferencesDataStore scanner returned ${preferences.size} preferences: ${preferences.map { it.key }}")
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = preferences,
                            isLoading = false
                        ))
                    } catch (e: Exception) {
                        android.util.Log.e("PreferencesRepository", "PreferencesDataStore scanning failed", e)
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = emptyList(),
                            isLoading = false,
                            error = e.message
                        ))
                    }
                }
            }
            PreferenceStorageType.PROTO_DATASTORE -> {
                flow {
                    emit(PreferenceGroup(
                        storageType = storageType,
                        isLoading = true
                    ))
                    try {
                        val preferences = protoDataStoreScanner.scanAllProtoDataStores()
                        android.util.Log.d("PreferencesRepository", "ProtoDataStore scanner returned ${preferences.size} preferences: ${preferences.map { it.key }}")
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = preferences,
                            isLoading = false
                        ))
                    } catch (e: Exception) {
                        android.util.Log.e("PreferencesRepository", "ProtoDataStore scanning failed", e)
                        emit(PreferenceGroup(
                            storageType = storageType,
                            preferences = emptyList(),
                            isLoading = false,
                            error = e.message
                        ))
                    }
                }
            }
        }
    }
    
    override fun getFilteredPreferences(filter: PreferenceFilter): Flow<List<AppPreference>> {
        return getAllPreferences().map { preferences ->
            preferences.filter { preference ->
                val matchesSearch = if (filter.searchQuery.isBlank()) {
                    true
                } else {
                    preference.key.contains(filter.searchQuery, ignoreCase = true) ||
                    preference.displayName.contains(filter.searchQuery, ignoreCase = true) ||
                    preference.value.toString().contains(filter.searchQuery, ignoreCase = true)
                }
                
                val matchesType = filter.typeFilter?.let { it == preference.type } ?: true
                
                val matchesStorageType = filter.storageTypeFilter?.let { it == preference.storageType } ?: true
                
                val matchesSystemFilter = if (filter.showSystemPreferences) {
                    true
                } else {
                    !preference.isSystemPreference
                }
                
                matchesSearch && matchesType && matchesStorageType && matchesSystemFilter
            }
        }
    }
    
    override suspend fun updatePreference(preference: AppPreference, newValue: Any) {
        when (preference.storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> {
                val parts = preference.key.split(".", limit = 2)
                if (parts.size == 2) {
                    val prefsFileName = parts[0]
                    val key = parts[1]
                    sharedPreferencesScanner.updateSharedPreference(prefsFileName, key, newValue, preference.type)
                        .getOrThrow()
                }
            }
            PreferenceStorageType.PREFERENCES_DATASTORE -> {
                preferencesDataStoreScanner.updatePreferencesDataStoreValue(preference.key, newValue, preference.type)
                    .getOrThrow()
            }
            PreferenceStorageType.PROTO_DATASTORE -> {
                protoDataStoreScanner.updateProtoPreference(preference, newValue)
                    .getOrThrow()
            }
        }
    }
    
    override suspend fun deletePreference(preference: AppPreference) {
        when (preference.storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> {
                val parts = preference.key.split(".", limit = 2)
                if (parts.size == 2) {
                    val prefsFileName = parts[0]
                    val key = parts[1]
                    sharedPreferencesScanner.deleteSharedPreference(prefsFileName, key)
                        .getOrThrow()
                }
            }
            PreferenceStorageType.PREFERENCES_DATASTORE -> {
                preferencesDataStoreScanner.deletePreferencesDataStoreKey(preference.key)
                    .getOrThrow()
            }
            PreferenceStorageType.PROTO_DATASTORE -> {
                protoDataStoreScanner.deleteProtoPreference(preference)
                    .getOrThrow()
            }
        }
    }
    
    override suspend fun clearPreferences(storageType: PreferenceStorageType) {
        when (storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> {
                // Clear all SharedPreferences files - this is a destructive operation
                val allPrefs = sharedPreferencesScanner.scanAllSharedPreferences()
                val prefsFiles = allPrefs.map { it.key.split(".")[0] }.distinct()
                prefsFiles.forEach { prefsFileName ->
                    sharedPreferencesScanner.clearSharedPreferences(prefsFileName)
                }
            }
            PreferenceStorageType.PREFERENCES_DATASTORE -> {
                preferencesDataStoreScanner.clearAllPreferencesDataStore()
                    .getOrThrow()
            }
            PreferenceStorageType.PROTO_DATASTORE -> {
                // Proto DataStore clearing would need individual file handling
                throw UnsupportedOperationException("Proto DataStore clearing requires individual file management")
            }
        }
    }
    
    override suspend fun clearAllPreferences() {
        clearPreferences(PreferenceStorageType.SHARED_PREFERENCES)
        clearPreferences(PreferenceStorageType.PREFERENCES_DATASTORE)
        // Proto DataStore left out due to complexity
    }
    
    override suspend fun exportPreferences(storageType: PreferenceStorageType?): String {
        val preferencesToExport = if (storageType != null) {
            getPreferencesByStorageType(storageType).map { it.preferences }.first()
        } else {
            getAllPreferences().first()
        }
        
        val exportData = mapOf(
            "version" to 1,
            "timestamp" to System.currentTimeMillis(),
            "storageType" to storageType?.name,
            "preferences" to preferencesToExport.map { pref ->
                mapOf(
                    "key" to pref.key,
                    "value" to pref.value,
                    "type" to pref.type.name,
                    "storageType" to pref.storageType.name
                )
            }
        )
        
        return gson.toJson(exportData)
    }
    
    override suspend fun importPreferences(data: String, targetStorageType: PreferenceStorageType): Result<Unit> {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val importData = gson.fromJson<Map<String, Any>>(data, type)
            
            @Suppress("UNCHECKED_CAST")
            val preferences = importData["preferences"] as? List<Map<String, Any>>
                ?: return Result.failure(IllegalArgumentException("Invalid preferences data"))
            
            preferences.forEach { prefMap ->
                val key = prefMap["key"] as? String ?: return@forEach
                val value = prefMap["value"] ?: return@forEach
                val typeString = prefMap["type"] as? String ?: return@forEach
                
                try {
                    val prefType = PreferenceType.valueOf(typeString)
                    addPreference(key, value, prefType, targetStorageType)
                } catch (e: Exception) {
                    // Skip invalid preference
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addPreference(key: String, value: Any, type: PreferenceType, storageType: PreferenceStorageType) {
        when (storageType) {
            PreferenceStorageType.SHARED_PREFERENCES -> {
                // For SharedPreferences, we need a file name. Use "jarvis_added" as default
                sharedPreferencesScanner.updateSharedPreference("jarvis_added", key, value, type)
                    .getOrThrow()
            }
            PreferenceStorageType.PREFERENCES_DATASTORE -> {
                preferencesDataStoreScanner.updatePreferencesDataStoreValue(key, value, type)
                    .getOrThrow()
            }
            PreferenceStorageType.PROTO_DATASTORE -> {
                throw UnsupportedOperationException("Adding Proto DataStore preferences requires schema definition")
            }
        }
    }
}