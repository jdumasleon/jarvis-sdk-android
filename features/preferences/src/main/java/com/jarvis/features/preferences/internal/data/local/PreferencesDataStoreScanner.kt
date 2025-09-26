@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.preferences.internal.data.local

import androidx.annotation.RestrictTo

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
import com.jarvis.features.preferences.internal.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.internal.data.config.PreferencesDataConfig
import com.jarvis.features.preferences.internal.domain.entity.AppPreference
import com.jarvis.features.preferences.internal.domain.entity.PreferenceStorageType
import com.jarvis.features.preferences.internal.domain.entity.PreferenceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStoreScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configProvider: PreferencesConfigProviderImpl
) {
    
    // Cache for dynamically created DataStore instances
    private val dataStoreCache = mutableMapOf<String, DataStore<Preferences>>()

    suspend fun scanAllPreferencesDataStores(): List<AppPreference> {
        return withContext(Dispatchers.IO) {
            try {
                val config = configProvider.getDataConfig()
                val preferences = mutableListOf<AppPreference>()
                
                android.util.Log.d("PreferencesDataStoreScanner", "Starting DataStore scan with config: autoDiscover=${config.autoDiscoverDataStores}, include=${config.includeDataStores}, exclude=${config.excludeDataStores}")
                
                // Step 1: Get DataStore names to scan
                val dataStoreNames = getDataStoreNamesToScan(config)
                android.util.Log.d("PreferencesDataStoreScanner", "DataStore names to scan: $dataStoreNames")
                
                // Step 2: Scan configured DataStores
                dataStoreNames.forEach { dataStoreName ->
                    try {
                        val dataStore = getOrCreateDataStore(dataStoreName)
                        val dataStorePreferences = dataStore.data.first()
                        
                        android.util.Log.d("PreferencesDataStoreScanner", "Found ${dataStorePreferences.asMap().size} preferences in $dataStoreName")
                        
                        dataStorePreferences.asMap().forEach { (key, value) ->
                            val preference = AppPreference(
                                key = "${dataStoreName}.${key.name}",
                                value = value,
                                type = determinePreferenceType(value),
                                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                                displayName = key.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                                description = "DataStore preference from $dataStoreName",
                                filePath = "${context.filesDir}/datastore/${dataStoreName}.preferences_pb",
                                isSystemPreference = isSystemDataStore(dataStoreName),
                                isEditable = config.enablePreferenceEditing
                            )
                            preferences.add(preference)
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("PreferencesDataStoreScanner", "Could not read DataStore: $dataStoreName", e)
                        
                        // If we can't read the DataStore but the file exists, report it as found but inaccessible
                        val dataStoreFile = File(context.filesDir, "datastore/${dataStoreName}.preferences_pb")
                        if (dataStoreFile.exists()) {
                            val preference = AppPreference(
                                key = "${dataStoreName}.file_info",
                                value = "DataStore file (${dataStoreFile.length()} bytes) - content not accessible",
                                type = PreferenceType.STRING,
                                storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                                displayName = "Inaccessible DataStore",
                                description = "DataStore file found at ${dataStoreFile.absolutePath} but content cannot be read",
                                filePath = dataStoreFile.absolutePath,
                                isSystemPreference = isSystemDataStore(dataStoreName),
                                isEditable = false
                            )
                            preferences.add(preference)
                        }
                    }
                }
                
                // Step 3: If auto-discovery is enabled, scan for unknown DataStore files
                if (config.autoDiscoverDataStores) {
                    val discoveredNames = discoverDataStoreFiles()
                    val unknownNames = discoveredNames - dataStoreNames.toSet()
                    
                    android.util.Log.d("PreferencesDataStoreScanner", "Auto-discovered DataStores: $discoveredNames, Unknown: $unknownNames")
                    
                    unknownNames.forEach { unknownName ->
                        if (!config.excludeDataStores.contains(unknownName)) {
                            try {
                                val dataStore = getOrCreateDataStore(unknownName)
                                val dataStorePreferences = dataStore.data.first()
                                
                                dataStorePreferences.asMap().forEach { (key, value) ->
                                    val preference = AppPreference(
                                        key = "${unknownName}.${key.name}",
                                        value = value,
                                        type = determinePreferenceType(value),
                                        storageType = PreferenceStorageType.PREFERENCES_DATASTORE,
                                        displayName = key.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                                        description = "Auto-discovered DataStore preference from $unknownName",
                                        filePath = "${context.filesDir}/datastore/${unknownName}.preferences_pb",
                                        isSystemPreference = isSystemDataStore(unknownName),
                                        isEditable = config.enablePreferenceEditing
                                    )
                                    preferences.add(preference)
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("PreferencesDataStoreScanner", "Could not read auto-discovered DataStore: $unknownName", e)
                            }
                        }
                    }
                }

                android.util.Log.d("PreferencesDataStoreScanner", "Scanning completed. Found ${preferences.size} preferences")
                preferences
            } catch (e: Exception) {
                android.util.Log.e("PreferencesDataStoreScanner", "Failed to scan Preferences DataStores", e)
                emptyList()
            }
        }
    }
    
    
    /**
     * Get the list of DataStore names to scan based on configuration
     */
    private fun getDataStoreNamesToScan(config: PreferencesDataConfig): List<String> {
        return when {
            config.includeDataStores.isNotEmpty() -> {
                // If explicit includes are specified, use only those
                config.includeDataStores.filter { !config.excludeDataStores.contains(it) }
            }
            else -> {
                // If no explicit includes, scan discovered files (if auto-discovery enabled)
                if (config.autoDiscoverDataStores) {
                    discoverDataStoreFiles().filter { !config.excludeDataStores.contains(it) }
                } else {
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Discover DataStore files in the filesystem
     */
    private fun discoverDataStoreFiles(): List<String> {
        val dataStoreDir = File(context.filesDir, "datastore")
        if (!dataStoreDir.exists()) {
            return emptyList()
        }
        
        return dataStoreDir.listFiles { file ->
            file.name.endsWith(".preferences_pb")
        }?.mapNotNull { file ->
            // Extract DataStore name from filename (remove .preferences_pb)
            val name = file.nameWithoutExtension
            if (name.endsWith(".preferences")) {
                name.removeSuffix(".preferences")
            } else {
                name
            }
        } ?: emptyList()
    }
    
    /**
     * Get or create a DataStore instance for the given name
     */
    private fun getOrCreateDataStore(name: String): DataStore<Preferences> {
        val config = configProvider.getDataConfig()
        
        // First, try to get from the registered instances (preferred approach)
        config.registeredDataStores[name]?.let { registeredDataStore ->
            android.util.Log.d("PreferencesDataStoreScanner", "Using registered DataStore for: $name")
            return registeredDataStore
        }
        
        // Fall back to creating our own instance if not registered
        android.util.Log.d("PreferencesDataStoreScanner", "Creating new DataStore instance for: $name (not registered)")
        return dataStoreCache.getOrPut(name) {
            createDataStoreForName(name)
        }
    }
    
    /**
     * Create a DataStore instance for a given name
     */
    private fun createDataStoreForName(name: String): DataStore<Preferences> {
        // We need to use the preferencesDataStore delegate factory
        // This is a bit tricky since we can't use the delegate syntax dynamically
        return androidx.datastore.preferences.core.PreferenceDataStoreFactory.create {
            File(context.filesDir, "datastore/${name}.preferences_pb")
        }
    }
    
    /**
     * Determine if a DataStore name represents a system preference
     */
    private fun isSystemDataStore(dataStoreName: String): Boolean {
        val config = configProvider.getDataConfig()
        if (!config.showSystemPreferences) {
            return dataStoreName.startsWith("androidx.") || 
                   dataStoreName.startsWith("android.") ||
                   dataStoreName.contains("system") ||
                   dataStoreName.contains("internal")
        }
        return false
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
            // Parse DataStore name and preference key from the composite key
            val (dataStoreName, prefKey) = parseCompositeKey(key)
            val dataStore = getOrCreateDataStore(dataStoreName)
            
            dataStore.edit { preferences ->
                    when (type) {
                        PreferenceType.STRING -> {
                            val keyInstance = stringPreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString()
                        }
                        PreferenceType.BOOLEAN -> {
                            val keyInstance = booleanPreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString().toBoolean()
                        }
                        PreferenceType.INTEGER -> {
                            val keyInstance = intPreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString().toInt()
                        }
                        PreferenceType.LONG -> {
                            val keyInstance = longPreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString().toLong()
                        }
                        PreferenceType.FLOAT -> {
                            val keyInstance = floatPreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString().toFloat()
                        }
                        PreferenceType.DOUBLE -> {
                            val keyInstance = doublePreferencesKey(prefKey)
                            preferences[keyInstance] = value.toString().toDouble()
                        }
                        PreferenceType.STRING_SET -> {
                            val keyInstance = stringSetPreferencesKey(prefKey)
                            val stringSet = if (value is Set<*>) {
                                value.map { it.toString() }.toSet()
                            } else {
                                setOf(value.toString())
                            }
                            preferences[keyInstance] = stringSet
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
            // Parse DataStore name and preference key from the composite key
            val (dataStoreName, prefKey) = parseCompositeKey(key)
            val dataStore = getOrCreateDataStore(dataStoreName)
            
            dataStore.edit { preferences ->
                    // Try different key types since we don't know the exact type
                    val stringKey = stringPreferencesKey(prefKey)
                    val booleanKey = booleanPreferencesKey(prefKey)
                    val intKey = intPreferencesKey(prefKey)
                    val floatKey = floatPreferencesKey(prefKey)
                    val longKey = longPreferencesKey(prefKey)
                    val doubleKey = doublePreferencesKey(prefKey)
                    val stringSetKey = stringSetPreferencesKey(prefKey)
                    
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
            // This method needs a DataStore name - we'll clear all discovered DataStores
            val config = configProvider.getDataConfig()
            val dataStoreNames = getDataStoreNamesToScan(config)
            
            dataStoreNames.forEach { dataStoreName ->
                val dataStore = getOrCreateDataStore(dataStoreName)
                dataStore.edit { preferences ->
                    preferences.clear()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Parse a composite key like "demo_preferences.user_name" into DataStore name and preference key
     */
    private fun parseCompositeKey(compositeKey: String): Pair<String, String> {
        val parts = compositeKey.split(".", limit = 2)
        return if (parts.size == 2) {
            Pair(parts[0], parts[1])
        } else {
            // Fallback - assume it's just a key for the first discovered DataStore
            val config = configProvider.getDataConfig()
            val dataStoreNames = getDataStoreNamesToScan(config)
            val defaultDataStoreName = dataStoreNames.firstOrNull() ?: "preferences"
            Pair(defaultDataStoreName, compositeKey)
        }
    }
    
}