package com.jarvis.api.error

/**
 * No-op error tracker for release builds.
 * All methods are empty to ensure zero overhead.
 */
object JarvisErrorTracker {
    
    @JvmStatic
    fun logError(
        throwable: Throwable,
        context: String? = null,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun logException(
        exception: Exception,
        isFatal: Boolean = false
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun recordBreadcrumb(
        message: String,
        category: String = "default"
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun setUser(
        userId: String,
        email: String? = null,
        username: String? = null
    ) {
        // No-op: Do nothing in release builds
    }
}