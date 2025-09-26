package com.jarvis.internal.feature.home.domain.entity

import androidx.annotation.RestrictTo

/**
 * Dashboard card types that can be displayed in the analytics grid
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class DashboardCardType(
    val title: String,
    val description: String
) {
    HEALTH_SUMMARY("Health Summary", "Overall app health score and key metrics"),
    PERFORMANCE_METRICS("Performance", "Real-time CPU, memory, and FPS metrics"),
    NETWORK_OVERVIEW("Network Analytics", "Requests timeline with detailed analytics"),
    PREFERENCES_OVERVIEW("Preferences", "App preferences and storage analytics"),
    HTTP_METHODS("HTTP Methods", "Request method distribution"),
    TOP_ENDPOINTS("Top Endpoints", "Most frequently used endpoints"),
    SLOW_ENDPOINTS("Slowest Endpoints", "Performance bottlenecks");
    
    companion object {
        fun getAllCards(): List<DashboardCardType> = values().toList()
    }
}