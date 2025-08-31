package com.jarvis.platform.api.analytics

/**
 * Analytics event for tracking user interactions
 */
data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null,
    val sessionId: String? = null
)

/**
 * User profile information for analytics
 */
data class UserProfile(
    val userId: String,
    val properties: Map<String, Any> = emptyMap(),
    val email: String? = null,
    val name: String? = null
)

/**
 * Analytics service interface
 */
interface Analytics {
    /**
     * Track an event
     */
    suspend fun track(event: AnalyticsEvent)
    
    /**
     * Track an event with name and properties
     */
    suspend fun track(eventName: String, properties: Map<String, Any> = emptyMap())
    
    /**
     * Identify a user
     */
    suspend fun identify(userProfile: UserProfile)
    
    /**
     * Set user properties
     */
    suspend fun setUserProperties(userProfile: UserProfile, properties: Map<String, Any>)
    
    /**
     * Enable or disable analytics
     */
    suspend fun setEnabled(enabled: Boolean)
    
    /**
     * Reset user data
     */
    suspend fun reset()
    
    /**
     * Flush pending events
     */
    suspend fun flush()
}