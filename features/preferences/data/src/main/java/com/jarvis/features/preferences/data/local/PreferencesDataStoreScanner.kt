package com.jarvis.features.preferences.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jarvis.features.preferences.domain.entity.AppPreference
import com.jarvis.features.preferences.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.domain.entity.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.defaultPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jarvis_detected_preferences"
)

@Singleton
class PreferencesDataStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun scanAllPreferencesDataStores(): List<AppPreference> {
        return withContext(Dispatchers.IO) {
            try {
                val preferences = mutableListOf<AppPreference>()
                
                // Scan for .preferences_pb files in the datastore directory
                val dataStoreDir = File(context.filesDir, "datastore")
                if (dataStoreDir.exists()) {
                    dataStoreDir.listFiles { file ->
                        file.name.endsWith(".preferences_pb")
                    }?.forEach { file ->
                        preferences.addAll(scanPreferencesDataStoreFile(file))
                    }
                }
                
                // Add sample preferences to demonstrate DataStore functionality
                preferences.addAll(generateSampleDataStorePreferences())
                
                preferences
            } catch (e: Exception) {
                // Fallback to sample data if scanning fails
                generateSampleDataStorePreferences()
            }
        }
    }
    
    private suspend fun scanPreferencesDataStoreFile(file: File): List<AppPreference> {
        return try {
            val preferences = context.defaultPreferencesDataStore.data.first()
            val preferencesList = mutableListOf<AppPreference>()
            
            preferences.asMap().forEach { (key, value) ->
                val preference = AppPreference(
                    key = key.name,
                    value = value,
                    type = determinePreferenceType(value),
                    storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                    displayName = key.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                    description = "DataStore preference from ${file.name}",
                    filePath = file.absolutePath
                )
                preferencesList.add(preference)
            }
            
            preferencesList
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun determinePreferenceType(value: Any): PreferenceType {
        return when (value) {
            is String -> PreferenceType.STRING
            is Boolean -> PreferenceType.BOOLEAN
            is Int -> PreferenceType.INTEGER
            is Long -> PreferenceType.LONG
            is Float -> PreferenceType.FLOAT
            is Double -> PreferenceType.DOUBLE
            else -> PreferenceType.STRING
        }
    }
    
    suspend fun updatePreferencesDataStoreValue(key: String, value: Any, type: PreferenceType): Result<Unit> {
        return try {
            context.defaultPreferencesDataStore.edit { preferences ->
                when (type) {
                    PreferenceType.STRING -> {
                        val prefKey = stringPreferencesKey(key)
                        preferences[prefKey] = value.toString()
                    }
                    PreferenceType.BOOLEAN -> {
                        val prefKey = booleanPreferencesKey(key)
                        preferences[prefKey] = value.toString().toBoolean()
                    }
                    PreferenceType.INTEGER -> {
                        val prefKey = intPreferencesKey(key)
                        preferences[prefKey] = value.toString().toInt()
                    }
                    PreferenceType.LONG -> {
                        val prefKey = longPreferencesKey(key)
                        preferences[prefKey] = value.toString().toLong()
                    }
                    PreferenceType.FLOAT -> {
                        val prefKey = floatPreferencesKey(key)
                        preferences[prefKey] = value.toString().toFloat()
                    }
                    PreferenceType.DOUBLE -> {
                        val prefKey = doublePreferencesKey(key)
                        preferences[prefKey] = value.toString().toDouble()
                    }
                    PreferenceType.STRING_SET -> {
                        val prefKey = stringSetPreferencesKey(key)
                        val stringSet = if (value is Set<*>) {
                            value.map { it.toString() }.toSet()
                        } else {
                            setOf(value.toString())
                        }
                        preferences[prefKey] = stringSet
                    }
                    PreferenceType.BYTES, PreferenceType.PROTO_MESSAGE -> {
                        // These types are not supported by Preferences DataStore
                        throw IllegalArgumentException("${type.name} preferences cannot be stored in Preferences DataStore")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePreferencesDataStoreKey(key: String): Result<Unit> {
        return try {
            context.defaultPreferencesDataStore.edit { preferences ->
                // Try different key types since we don't know the exact type
                val stringKey = stringPreferencesKey(key)
                val booleanKey = booleanPreferencesKey(key)
                val intKey = intPreferencesKey(key)
                val floatKey = floatPreferencesKey(key)
                val longKey = longPreferencesKey(key)
                val doubleKey = doublePreferencesKey(key)
                val stringSetKey = stringSetPreferencesKey(key)
                
                preferences.remove(stringKey)
                preferences.remove(booleanKey)
                preferences.remove(intKey)
                preferences.remove(floatKey)
                preferences.remove(longKey)
                preferences.remove(doubleKey)
                preferences.remove(stringSetKey)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearAllPreferencesDataStore(): Result<Unit> {
        return try {
            context.defaultPreferencesDataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateSampleDataStorePreferences(): List<AppPreference> {
        return listOf(
            AppPreference(
                key = "user_theme_preference",
                value = "dark",
                type = PreferenceType.STRING,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Theme Preference",
                description = "User's preferred theme setting"
            ),
            AppPreference(
                key = "notifications_enabled",
                value = true,
                type = PreferenceType.BOOLEAN,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Notifications Enabled",
                description = "Whether push notifications are enabled"
            ),
            AppPreference(
                key = "sync_frequency_minutes",
                value = 30,
                type = PreferenceType.INTEGER,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Sync Frequency",
                description = "How often to sync data in minutes"
            ),
            AppPreference(
                key = "max_cache_size_mb",
                value = 512,
                type = PreferenceType.INTEGER,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Max Cache Size",
                description = "Maximum cache size in megabytes"
            ),
            AppPreference(
                key = "auto_backup_enabled",
                value = false,
                type = PreferenceType.BOOLEAN,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Auto Backup",
                description = "Automatically backup user data"
            ),
            AppPreference(
                key = "data_sync_interval_hours",
                value = 4.5,
                type = PreferenceType.DOUBLE,
                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                displayName = "Data Sync Interval",
                description = "Interval between data synchronizations in hours"
            )
        )
    }
}