package com.jarvis.features.preferences.data.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Local configuration for preferences data scanning and management
 */
data class PreferencesDataConfig(
    // DataStore configuration
    val autoDiscoverDataStores: Boolean = true,
    val includeDataStores: List<String> = emptyList(),
    val excludeDataStores: List<String> = emptyList(),
    
    // SharedPreferences configuration  
    val autoDiscoverSharedPrefs: Boolean = true,
    val includeSharedPrefs: List<String> = emptyList(),
    val excludeSharedPrefs: List<String> = emptyList(),
    
    // Proto DataStore configuration
    val autoDiscoverProtoDataStores: Boolean = true,
    val includeProtoDataStores: List<String> = emptyList(),
    val excludeProtoDataStores: List<String> = emptyList(),
    
    // DataStore instance registration
    val registeredDataStores: Map<String, DataStore<Preferences>> = emptyMap(),
    val registeredProtoDataStores: Map<String, DataStore<*>> = emptyMap(),
    val protoExtractors: Map<String, (Any) -> Map<String, Any>> = emptyMap(),
    
    // General settings
    val maxFileSize: Long = 10 * 1024 * 1024, // 10MB max file size for scanning
    val enablePreferenceEditing: Boolean = true,
    val showSystemPreferences: Boolean = false
)