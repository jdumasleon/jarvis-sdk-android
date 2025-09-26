package com.jarvis.config

import com.jarvis.features.preferences.internal.domain.config.PreferencesConfiguration
import com.jarvis.features.preferences.internal.domain.config.PreferencesConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronizes SDK configuration with feature-specific configurations
 */
@Singleton
class ConfigurationSynchronizer @Inject constructor(
    private val preferencesConfigProvider: PreferencesConfigProvider
) {
    
    /**
     * Update all feature configurations based on the main SDK configuration
     */
    fun updateConfigurations(jarvisConfig: JarvisConfig) {
        val sdkPrefsConfig = jarvisConfig.preferences
        
        // Convert SDK config to domain preferences configuration
        val preferencesConfiguration = PreferencesConfiguration(
            autoDiscoverDataStores = sdkPrefsConfig.autoDiscoverDataStores,
            includeDataStores = sdkPrefsConfig.includeDataStores,
            excludeDataStores = sdkPrefsConfig.excludeDataStores,
            autoDiscoverSharedPrefs = sdkPrefsConfig.autoDiscoverSharedPrefs,
            includeSharedPrefs = sdkPrefsConfig.includeSharedPrefs,
            excludeSharedPrefs = sdkPrefsConfig.excludeSharedPrefs,
            autoDiscoverProtoDataStores = sdkPrefsConfig.autoDiscoverProtoDataStores,
            includeProtoDataStores = sdkPrefsConfig.includeProtoDataStores,
            excludeProtoDataStores = sdkPrefsConfig.excludeProtoDataStores,
            registeredDataStores = sdkPrefsConfig.registeredDataStores,
            registeredProtoDataStores = sdkPrefsConfig.registeredProtoDataStores,
            protoExtractors = sdkPrefsConfig.protoExtractors,
            maxFileSize = sdkPrefsConfig.maxFileSize,
            enablePreferenceEditing = sdkPrefsConfig.enablePreferenceEditing,
            showSystemPreferences = sdkPrefsConfig.showSystemPreferences
        )
        
        // Update configuration through domain interface
        preferencesConfigProvider.updateConfiguration(preferencesConfiguration)
        
        android.util.Log.d("ConfigurationSynchronizer", "Updated preferences config: autoDiscover=${preferencesConfiguration.autoDiscoverDataStores}, include=${preferencesConfiguration.includeDataStores}")
    }
}