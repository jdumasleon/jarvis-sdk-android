package com.jarvis.api.network

/**
 * No-op network logger for release builds.
 * All methods are empty to ensure zero overhead.
 */
object JarvisNetworkLogger {
    
    @JvmStatic
    fun logRequest(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun logResponse(
        requestId: String,
        statusCode: Int,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        duration: Long = 0L
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun logError(
        requestId: String,
        error: Throwable,
        duration: Long = 0L
    ) {
        // No-op: Do nothing in release builds
    }
}