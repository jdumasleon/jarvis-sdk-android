package com.jarvis.internal.feature.home.domain.entity

/**
 * Dashboard metrics aggregated from different Jarvis features
 */
data class DashboardMetrics(
    val networkMetrics: NetworkMetrics,
    val preferencesMetrics: PreferencesMetrics,
    val performanceMetrics: PerformanceMetrics
)

/**
 * Enhanced dashboard metrics with advanced analytics
 */
data class EnhancedDashboardMetrics(
    // Legacy metrics for backward compatibility
    val networkMetrics: NetworkMetrics,
    val preferencesMetrics: PreferencesMetrics,
    val performanceMetrics: PerformanceMetrics,
    
    // Enhanced metrics
    val healthScore: HealthScore?,
    val enhancedNetworkMetrics: EnhancedNetworkMetrics,
    val enhancedPreferencesMetrics: EnhancedPreferencesMetrics,
    
    // Session info
    val sessionInfo: SessionInfo?,
    val lastUpdated: Long = System.currentTimeMillis()
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


// Mock objects for testing and previews
object DashboardMetricsMocks {
    val mockNetworkMetrics = NetworkMetrics(
        totalCalls = 247,
        averageSpeed = 156.8,
        successfulCalls = 231,
        failedCalls = 16,
        successRate = 93.5,
        averageRequestSize = 2048,
        averageResponseSize = 4096,
        mostUsedEndpoint = "GET /api/users",
        p50 = 120.0,
        p90 = 280.0,
        p95 = 420.0,
        p99 = 850.0,
        topSlowEndpoints = listOf("GET /api/reports", "POST /api/upload", "GET /api/analytics")
    )
    
    val mockPreferencesMetrics = PreferencesMetrics(
        totalPreferences = 42,
        preferencesByType = mapOf(
            "SHARED_PREFERENCES" to 25,
            "DATASTORE" to 12,
            "PROTO" to 5
        ),
        mostCommonType = "SHARED_PREFERENCES",
        lastModified = System.currentTimeMillis() - 3600000
    )
    
    val mockPerformanceMetrics = PerformanceMetrics(
        overallRating = PerformanceRating.GOOD,
        averageResponseTime = 156.8,
        slowestCall = 2850.0,
        fastestCall = 45.0,
        errorRate = 6.5,
        p95 = 420.0,
        apdex = 0.82
    )
    
    val mockSessionInfo = SessionInfo(
        sessionId = "session_123",
        startTime = System.currentTimeMillis() - 1800000,
        endTime = null,
        isCurrentSession = true
    )
}

// Extension for mock enhanced dashboard metrics
object EnhancedDashboardMetricsMock {
    val mockEnhancedDashboardMetrics: EnhancedDashboardMetrics
        get() = EnhancedDashboardMetrics(
            networkMetrics = DashboardMetricsMocks.mockNetworkMetrics,
            preferencesMetrics = DashboardMetricsMocks.mockPreferencesMetrics,
            performanceMetrics = DashboardMetricsMocks.mockPerformanceMetrics,
            healthScore = HealthScoreMock.mockHealthScore,
            enhancedNetworkMetrics = EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics,
            enhancedPreferencesMetrics = EnhancedPreferencesMetricsMock.mockEnhancedPreferencesMetrics,
            sessionInfo = DashboardMetricsMocks.mockSessionInfo,
            lastUpdated = System.currentTimeMillis()
        )
}