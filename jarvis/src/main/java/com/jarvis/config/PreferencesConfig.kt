package com.jarvis.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.protobuf.MessageLite

/**
 * Configuration for preferences scanning and management
 */
data class PreferencesConfig(
    // DataStore configuration
    val autoDiscoverDataStores: Boolean = true,
    val includeDataStores: List<String> = emptyList(),
    val excludeDataStores: List<String> = listOf(
        "platform_preferences",        // SDK internal platform preferences
        "jarvis_internal_preferences"  // SDK internal preferences (header state, etc.)
    ),
    
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
) {
    class Builder {
        private var autoDiscoverDataStores: Boolean = true
        private var includeDataStores: List<String> = emptyList()
        private var excludeDataStores: List<String> = emptyList()
        
        private var autoDiscoverSharedPrefs: Boolean = true
        private var includeSharedPrefs: List<String> = emptyList()
        private var excludeSharedPrefs: List<String> = emptyList()
        
        private var autoDiscoverProtoDataStores: Boolean = true
        private var includeProtoDataStores: List<String> = emptyList()
        private var excludeProtoDataStores: List<String> = emptyList()
        
        private var registeredDataStores: MutableMap<String, DataStore<Preferences>> = mutableMapOf()
        private var registeredProtoDataStores: MutableMap<String, DataStore<*>> = mutableMapOf()
        private var protoExtractors: MutableMap<String, (Any) -> Map<String, Any>> = mutableMapOf()
        
        private var maxFileSize: Long = 10 * 1024 * 1024
        private var enablePreferenceEditing: Boolean = true
        private var showSystemPreferences: Boolean = false

        // DataStore configuration methods
        fun autoDiscoverDataStores(enabled: Boolean): Builder {
            autoDiscoverDataStores = enabled
            return this
        }

        fun includeDataStores(vararg names: String): Builder {
            includeDataStores = names.toList()
            return this
        }

        fun includeDataStores(names: List<String>): Builder {
            includeDataStores = names
            return this
        }

        fun excludeDataStores(vararg names: String): Builder {
            excludeDataStores = names.toList()
            return this
        }

        fun excludeDataStores(names: List<String>): Builder {
            excludeDataStores = names
            return this
        }

        // SharedPreferences configuration methods
        fun autoDiscoverSharedPrefs(enabled: Boolean): Builder {
            autoDiscoverSharedPrefs = enabled
            return this
        }

        fun includeSharedPrefs(vararg names: String): Builder {
            includeSharedPrefs = names.toList()
            return this
        }

        fun includeSharedPrefs(names: List<String>): Builder {
            includeSharedPrefs = names
            return this
        }

        fun excludeSharedPrefs(vararg names: String): Builder {
            excludeSharedPrefs = names.toList()
            return this
        }

        fun excludeSharedPrefs(names: List<String>): Builder {
            excludeSharedPrefs = names
            return this
        }

        // Proto DataStore configuration methods
        fun autoDiscoverProtoDataStores(enabled: Boolean): Builder {
            autoDiscoverProtoDataStores = enabled
            return this
        }

        fun includeProtoDataStores(vararg names: String): Builder {
            includeProtoDataStores = names.toList()
            return this
        }

        fun includeProtoDataStores(names: List<String>): Builder {
            includeProtoDataStores = names
            return this
        }

        fun excludeProtoDataStores(vararg names: String): Builder {
            excludeProtoDataStores = names.toList()
            return this
        }

        fun excludeProtoDataStores(names: List<String>): Builder {
            excludeProtoDataStores = names
            return this
        }

        // General settings
        fun maxFileSize(sizeInBytes: Long): Builder {
            maxFileSize = sizeInBytes
            return this
        }

        fun enablePreferenceEditing(enabled: Boolean): Builder {
            enablePreferenceEditing = enabled
            return this
        }

        fun showSystemPreferences(enabled: Boolean): Builder {
            showSystemPreferences = enabled
            return this
        }

        // DataStore instance registration methods
        fun registerDataStore(name: String, dataStore: DataStore<Preferences>): Builder {
            registeredDataStores[name] = dataStore
            return this
        }

        fun <T : MessageLite> registerProtoDataStore(
            name: String,
            dataStore: DataStore<T>,
            extractor: (T) -> Map<String, Any>
        ): Builder {
            registeredProtoDataStores[name] = dataStore
            @Suppress("UNCHECKED_CAST")
            protoExtractors[name] = extractor as (Any) -> Map<String, Any>
            return this
        }

        fun build(): PreferencesConfig = PreferencesConfig(
            autoDiscoverDataStores = autoDiscoverDataStores,
            includeDataStores = includeDataStores,
            excludeDataStores = excludeDataStores,
            autoDiscoverSharedPrefs = autoDiscoverSharedPrefs,
            includeSharedPrefs = includeSharedPrefs,
            excludeSharedPrefs = excludeSharedPrefs,
            autoDiscoverProtoDataStores = autoDiscoverProtoDataStores,
            includeProtoDataStores = includeProtoDataStores,
            excludeProtoDataStores = excludeProtoDataStores,
            registeredDataStores = registeredDataStores,
            registeredProtoDataStores = registeredProtoDataStores,
            protoExtractors = protoExtractors,
            maxFileSize = maxFileSize,
            enablePreferenceEditing = enablePreferenceEditing,
            showSystemPreferences = showSystemPreferences
        )
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}