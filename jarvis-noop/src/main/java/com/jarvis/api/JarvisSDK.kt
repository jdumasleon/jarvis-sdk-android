package com.jarvis.api

import android.content.Context
import androidx.activity.ComponentActivity

/**
 * No-op implementation of JarvisSDK for release builds.
 * This provides the same API but with empty implementations to ensure zero overhead.
 */
class JarvisSDK private constructor() {
    
    companion object {
        @JvmStatic
        fun initialize(context: Context): JarvisSDK {
            // No-op: Return instance but do nothing
            return JarvisSDK()
        }
        
        @JvmStatic
        fun getInstance(): JarvisSDK {
            return JarvisSDK()
        }
    }
    
    // Core lifecycle methods - all no-op
    fun initialize(hostActivity: ComponentActivity) {
        // No-op: Do nothing in release builds
    }
    
    fun activate() {
        // No-op: Do nothing in release builds
    }
    
    fun deactivate() {
        // No-op: Do nothing in release builds
    }
    
    fun dismiss() {
        // No-op: Do nothing in release builds
    }
    
    fun toggle(): Boolean {
        // No-op: Always return false (inactive)
        return false
    }
    
    fun isActive(): Boolean {
        // No-op: Always return false
        return false
    }
    
    // Configuration methods - all no-op
    fun updateConfiguration(config: JarvisConfig) {
        // No-op: Do nothing in release builds
    }
    
    fun getConfiguration(): JarvisConfig {
        // No-op: Return default config
        return JarvisConfig()
    }
    
    // Feature registration - all no-op
    fun registerFeature(feature: Any) {
        // No-op: Do nothing in release builds
    }
    
    // Data export methods - all no-op
    fun exportNetworkLogs(): String {
        // No-op: Return empty data
        return "{}"
    }
    
    fun exportPreferences(): String {
        // No-op: Return empty data
        return "{}"
    }
    
    fun exportAllData(): String {
        // No-op: Return empty data
        return "{}"
    }
}