package com.jarvis.core.platform

import com.jarvis.core.platform.analytics.Analytics
import com.jarvis.core.platform.analytics.UserProfile
import com.jarvis.core.platform.crash.CrashReporter
import com.jarvis.core.platform.featureflags.FeatureFlags
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main platform service providing access to all platform services
 */
@Singleton
class JarvisPlatform @Inject constructor(
    val analytics: Analytics,
    val crashReporter: CrashReporter,
    val featureFlags: FeatureFlags
) {
    
    private var isInitialized = false
    
    /**
     * Initialize all platform services
     * This should be called once during app startup
     */
    suspend fun initialize() {
        if (isInitialized) return
        
        try {
            // Initialize crash reporter first to catch any initialization errors
            crashReporter.initialize()
            crashReporter.log("Platform initialization started")
            
            // Initialize feature flags
            featureFlags.initialize()
            
            // Analytics doesn't need explicit initialization as it's handled by PostHog
            
            isInitialized = true
            
            // Log successful initialization
            crashReporter.log("Platform initialization completed successfully")
            analytics.track("platform_initialized")
            
        } catch (exception: Exception) {
            crashReporter.recordException(exception, mapOf("context" to "platform_initialization"))
            throw exception
        }
    }
    
    /**
     * Check if platform is initialized
     */
    fun isInitialized(): Boolean = isInitialized
    
    /**
     * Enable or disable platform services
     */
    suspend fun setEnabled(enabled: Boolean) {
        if (!isInitialized) {
            throw IllegalStateException("Platform must be initialized before enabling/disabling")
        }
        
        analytics.setEnabled(enabled)
        crashReporter.setEnabled(enabled)
        
        crashReporter.log("Platform services ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            analytics.track("platform_enabled")
        } else {
            analytics.track("platform_disabled")
        }
    }
    
    /**
     * Track app lifecycle events
     */
    suspend fun onAppStart() {
        if (isInitialized) {
            analytics.track("app_started")
            crashReporter.addBreadcrumb("App started", "lifecycle")
        }
    }
    
    suspend fun onAppStop() {
        if (isInitialized) {
            analytics.track("app_stopped")
            crashReporter.addBreadcrumb("App stopped", "lifecycle")
            analytics.flush() // Ensure events are sent before app stops
        }
    }
    
    /**
     * Set user information across all platform services
     */
    suspend fun setUser(
        userId: String,
        email: String? = null,
        username: String? = null,
        properties: Map<String, Any> = emptyMap()
    ) {
        if (!isInitialized) return
        
        // Set user in crash reporter
        crashReporter.setUser(userId, email, username)
        
        // Set user in analytics
        val userProfile = UserProfile(
            userId = userId,
            email = email,
            properties = properties + mapOfNotNull(
                "name" to username
            )
        )
        analytics.identify(userProfile)
    }
    
    private fun mapOfNotNull(vararg pairs: Pair<String, Any?>): Map<String, Any> {
        return pairs.mapNotNull { (key, value) ->
            value?.let { key to it }
        }.toMap()
    }
}