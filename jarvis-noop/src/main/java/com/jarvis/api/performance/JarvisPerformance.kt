package com.jarvis.api.performance

/**
 * No-op performance tracker for release builds.
 * All methods are empty to ensure zero overhead.
 */
object JarvisPerformance {
    
    @JvmStatic
    fun trackEvent(
        name: String,
        properties: Map<String, Any> = emptyMap()
    ) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun measureTime(operationName: String, operation: () -> Unit) {
        // No-op: Just execute the operation without measurement
        operation()
    }
    
    @JvmStatic
    fun startTimer(name: String) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun stopTimer(name: String) {
        // No-op: Do nothing in release builds
    }
    
    @JvmStatic
    fun recordMetric(name: String, value: Double) {
        // No-op: Do nothing in release builds
    }
}