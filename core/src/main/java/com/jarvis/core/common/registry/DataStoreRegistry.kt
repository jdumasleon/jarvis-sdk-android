package com.jarvis.core.common.registry

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.protobuf.MessageLite

/**
 * Global registry for DataStore instances to enable Jarvis SDK scanning
 * 
 * This registry allows applications to register their DataStore instances
 * so that the Jarvis SDK can access them directly instead of creating new instances,
 * which avoids concurrency and access issues.
 */
object DataStoreRegistry {
    
    private val preferencesDataStores = mutableMapOf<String, DataStore<Preferences>>()
    private val protoDataStores = mutableMapOf<String, DataStore<*>>()
    private val protoExtractors = mutableMapOf<String, (Any) -> Map<String, Any>>()
    
    /**
     * Register a Preferences DataStore instance
     */
    fun registerPreferencesDataStore(name: String, dataStore: DataStore<Preferences>) {
        preferencesDataStores[name] = dataStore
        android.util.Log.d("DataStoreRegistry", "Registered Preferences DataStore: $name")
    }
    
    /**
     * Register a Proto DataStore instance with a custom extractor function
     */
    fun <T : MessageLite> registerProtoDataStore(
        name: String, 
        dataStore: DataStore<T>,
        extractor: (T) -> Map<String, Any>
    ) {
        protoDataStores[name] = dataStore
        @Suppress("UNCHECKED_CAST")
        protoExtractors[name] = extractor as (Any) -> Map<String, Any>
        android.util.Log.d("DataStoreRegistry", "Registered Proto DataStore: $name")
    }
    
    /**
     * Get a registered Preferences DataStore
     */
    fun getPreferencesDataStore(name: String): DataStore<Preferences>? {
        return preferencesDataStores[name]
    }
    
    /**
     * Get a registered Proto DataStore
     */
    fun getProtoDataStore(name: String): DataStore<*>? {
        return protoDataStores[name]
    }
    
    /**
     * Get the extractor function for a Proto DataStore
     */
    fun getProtoExtractor(name: String): ((Any) -> Map<String, Any>)? {
        return protoExtractors[name]
    }
    
    /**
     * Get all registered Preferences DataStore names
     */
    fun getRegisteredPreferencesDataStores(): Set<String> {
        return preferencesDataStores.keys.toSet()
    }
    
    /**
     * Get all registered Proto DataStore names  
     */
    fun getRegisteredProtoDataStores(): Set<String> {
        return protoDataStores.keys.toSet()
    }
    
    /**
     * Check if a Preferences DataStore is registered
     */
    fun isPreferencesDataStoreRegistered(name: String): Boolean {
        return preferencesDataStores.containsKey(name)
    }
    
    /**
     * Check if a Proto DataStore is registered
     */
    fun isProtoDataStoreRegistered(name: String): Boolean {
        return protoDataStores.containsKey(name)
    }
    
    /**
     * Clear all registered DataStores (for testing)
     */
    internal fun clear() {
        preferencesDataStores.clear()
        protoDataStores.clear()
        protoExtractors.clear()
    }
}