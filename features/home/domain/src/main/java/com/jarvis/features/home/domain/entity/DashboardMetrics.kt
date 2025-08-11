package com.jarvis.features.home.domain.entity

/**
 * Dashboard metrics aggregated from different Jarvis features
 */
data class DashboardMetrics(
    val networkMetrics: NetworkMetrics,
    val preferencesMetrics: PreferencesMetrics,
    val performanceMetrics: PerformanceMetrics
)

/**
 * Network-related metrics from Inspector feature
 */
data class NetworkMetrics(
    val totalCalls: Int,
    val averageSpeed: Double,              // ms (mean)
    val successfulCalls: Int,
    val failedCalls: Int,
    val successRate: Double,               // % (0..100)
    val averageRequestSize: Long,          // bytes
    val averageResponseSize: Long,         // bytes
    val mostUsedEndpoint: String?,
    val p50: Double,                       // ms
    val p90: Double,                       // ms
    val p95: Double,                       // ms
    val p99: Double,                       // ms
    val topSlowEndpoints: List<String>     // e.g., "GET /users/{id}"
)

/**
 * Preferences-related metrics from Preferences Inspector feature
 */
data class PreferencesMetrics(
    val totalPreferences: Int,
    val preferencesByType: Map<String, Int>, // storage type -> count
    val mostCommonType: String?,
    val lastModified: Long?                  // timestamp
)

/**
 * Performance metrics calculated from network data
 */
data class PerformanceMetrics(
    val overallRating: PerformanceRating,
    val averageResponseTime: Double, // ms (mean)
    val slowestCall: Double?,        // ms
    val fastestCall: Double?,        // ms
    val errorRate: Double,           // % (0..100)
    val p95: Double,                 // ms
    val apdex: Double                // 0.0..1.0
)

enum class PerformanceRating {
    EXCELLENT, GOOD, AVERAGE, POOR, CRITICAL
}