package com.jarvis.features.home.domain.entity

/**
 * Session filter options for dashboard data
 */
enum class SessionFilter {
    LAST_SESSION,  // Current app session only
    GENERAL        // All historical data
}

/**
 * Session info for filtering dashboard metrics
 */
data class SessionInfo(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long?,
    val isCurrentSession: Boolean = endTime == null
)