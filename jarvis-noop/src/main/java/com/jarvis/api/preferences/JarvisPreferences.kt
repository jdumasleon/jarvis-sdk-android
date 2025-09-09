package com.jarvis.api.preferences

/**
 * No-op preferences manager for release builds.
 * All methods are empty to ensure zero overhead.
 */
object JarvisPreferences {
    
    @JvmStatic
    fun register(
        storageType: String,
        preferences: Map<String, Any>
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun observe(callback: (String, Any?, Any?) -> Unit) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun refresh() {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun export(): String {
        // No-op: Return empty data
        return "{}"
    }
}