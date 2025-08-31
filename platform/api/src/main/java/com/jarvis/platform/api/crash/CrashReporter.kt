package com.jarvis.platform.api.crash

/**
 * Crash reporter interface for error tracking and monitoring
 */
interface CrashReporter {
    /**
     * Initialize the crash reporter
     */
    suspend fun initialize()
    
    /**
     * Record an exception
     */
    suspend fun recordException(throwable: Throwable, tags: Map<String, String> = emptyMap())
    
    /**
     * Log a message
     */
    suspend fun log(message: String, level: LogLevel = LogLevel.INFO)
    
    /**
     * Add user context
     */
    suspend fun setUser(userId: String, email: String? = null, username: String? = null)
    
    /**
     * Set custom tag
     */
    suspend fun setTag(key: String, value: String)
    
    /**
     * Set multiple tags
     */
    suspend fun setTags(tags: Map<String, String>)
    
    /**
     * Add breadcrumb
     */
    suspend fun addBreadcrumb(message: String, category: String = "general", level: LogLevel = LogLevel.INFO)
    
    /**
     * Set custom context
     */
    suspend fun setContext(key: String, context: Map<String, Any>)
    
    /**
     * Enable or disable crash reporting
     */
    suspend fun setEnabled(enabled: Boolean)
}

/**
 * Log levels for crash reporting
 */
enum class LogLevel(val value: String) {
    DEBUG("debug"),
    INFO("info"),
    WARNING("warning"),
    ERROR("error"),
    FATAL("fatal")
}