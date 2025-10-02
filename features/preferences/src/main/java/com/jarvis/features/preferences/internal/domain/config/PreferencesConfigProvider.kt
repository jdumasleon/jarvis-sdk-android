@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.preferences.internal.domain.config

import androidx.annotation.RestrictTo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Configuration for preferences scanning and management (Domain layer)
 */
data class PreferencesConfiguration(
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
    val maxFileSize: Long = 10 * 1024 * 1024, // 10MB max file size
    val enablePreferenceEditing: Boolean = true,
    val showSystemPreferences: Boolean = false
)

/**
 * Provider interface for preferences configuration (Domain layer)
 */
interface PreferencesConfigProvider {
    
    /**
     * Update the preferences configuration
     */
    fun updateConfiguration(config: PreferencesConfiguration)
    
    /**
     * Get the current preferences configuration
     */
    fun getConfiguration(): PreferencesConfiguration
}