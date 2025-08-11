package com.jarvis.features.preferences.data.config

import com.jarvis.features.preferences.domain.config.PreferencesConfiguration
import com.jarvis.features.preferences.domain.config.PreferencesConfigProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PreferencesConfigProvider (Data layer)
 */
@Singleton
class PreferencesConfigProviderImpl @Inject constructor() : PreferencesConfigProvider {
    
    @Volatile
    private var configuration = PreferencesConfiguration()
    
    override fun updateConfiguration(config: PreferencesConfiguration) {
        configuration = config
        android.util.Log.d("PreferencesConfigProvider", "Configuration updated: autoDiscover=${config.autoDiscoverDataStores}, include=${config.includeDataStores}")
    }
    
    override fun getConfiguration(): PreferencesConfiguration {
        return configuration
    }
    
    /**
     * Get configuration in data layer format for internal use
     */
    fun getDataConfig(): PreferencesDataConfig {
        return PreferencesDataConfig(
            autoDiscoverDataStores = configuration.autoDiscoverDataStores,
            includeDataStores = configuration.includeDataStores,
            excludeDataStores = configuration.excludeDataStores,
            autoDiscoverSharedPrefs = configuration.autoDiscoverSharedPrefs,
            includeSharedPrefs = configuration.includeSharedPrefs,
            excludeSharedPrefs = configuration.excludeSharedPrefs,
            autoDiscoverProtoDataStores = configuration.autoDiscoverProtoDataStores,
            includeProtoDataStores = configuration.includeProtoDataStores,
            excludeProtoDataStores = configuration.excludeProtoDataStores,
            registeredDataStores = configuration.registeredDataStores,
            registeredProtoDataStores = configuration.registeredProtoDataStores,
            protoExtractors = configuration.protoExtractors,
            maxFileSize = configuration.maxFileSize,
            enablePreferenceEditing = configuration.enablePreferenceEditing,
            showSystemPreferences = configuration.showSystemPreferences
        )
    }
}