@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.platform.featureflags

import androidx.annotation.RestrictTo

import kotlinx.coroutines.flow.Flow

/**
 * Feature flag interface for managing feature toggles and experiments
 */
interface FeatureFlags {
    /**
     * Initialize feature flags
     */
    suspend fun initialize()
    
    /**
     * Check if a feature is enabled
     */
    suspend fun isFeatureEnabled(key: String): Boolean
    
    /**
     * Get feature flag value as string
     */
    suspend fun getStringFlag(key: String, defaultValue: String = ""): String
    
    /**
     * Get feature flag value as boolean
     */
    suspend fun getBooleanFlag(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Get feature flag value as integer
     */
    suspend fun getIntFlag(key: String, defaultValue: Int = 0): Int
    
    /**
     * Get feature flag value as double
     */
    suspend fun getDoubleFlag(key: String, defaultValue: Double = 0.0): Double
    
    /**
     * Get feature flag value as JSON object
     */
    suspend fun getJsonFlag(key: String, defaultValue: Map<String, Any> = emptyMap()): Map<String, Any>
    
    /**
     * Reload feature flags from remote
     */
    suspend fun reload()
    
    /**
     * Get all feature flags as a flow
     */
    fun getAllFlags(): Flow<Map<String, Any>>
    
    /**
     * Override a flag locally (for testing)
     */
    suspend fun overrideFlag(key: String, value: Any)
    
    /**
     * Clear all local overrides
     */
    suspend fun clearOverrides()
}