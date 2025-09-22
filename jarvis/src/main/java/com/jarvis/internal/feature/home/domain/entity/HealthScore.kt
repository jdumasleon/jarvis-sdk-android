@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.domain.entity

import androidx.annotation.RestrictTo

/**
 * Overall health score and key metrics for the application
 */
data class HealthScore(
    val overallScore: Float,           // 0.0 to 100.0
    val rating: HealthRating,
    val keyMetrics: HealthKeyMetrics,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Health rating categories
 */
enum class HealthRating(val displayName: String, val color: String) {
    EXCELLENT("Excellent", "#4CAF50"),
    GOOD("Good", "#8BC34A"),
    AVERAGE("Average", "#FFC107"),
    POOR("Poor", "#FF9800"),
    CRITICAL("Critical", "#F44336")
}

/**
 * Key health metrics displayed in the summary
 */
data class HealthKeyMetrics(
    val totalRequests: Int,
    val errorRate: Float,              // Percentage 0.0-100.0
    val averageResponseTime: Float,    // milliseconds
    val performanceScore: Float,       // 0.0-100.0
    val networkScore: Float,           // 0.0-100.0
    val uptime: Float                  // Percentage 0.0-100.0
)

/**
 * Factors contributing to health score calculation
 */
data class HealthScoreFactors(
    val networkPerformance: Float,     // Weight: 40%
    val errorRate: Float,              // Weight: 30%
    val responseTime: Float,           // Weight: 20%
    val systemResources: Float         // Weight: 10%
)

// Mock objects for testing and previews
object HealthScoreMock {
    val mockHealthScore: HealthScore
        get() = HealthScore(
            overallScore = 85.6f,
            rating = HealthRating.GOOD,
            keyMetrics = HealthKeyMetrics(
                totalRequests = 247,
                errorRate = 6.5f,
                averageResponseTime = 156.8f,
                performanceScore = 82.3f,
                networkScore = 88.1f,
                uptime = 99.2f
            ),
            lastUpdated = System.currentTimeMillis()
        )
}