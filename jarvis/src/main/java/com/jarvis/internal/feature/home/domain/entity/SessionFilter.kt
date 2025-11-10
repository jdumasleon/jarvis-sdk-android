@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.domain.entity

import androidx.annotation.RestrictTo

/**
 * Session filter options for dashboard data
 */
enum class SessionFilter {
    LAST_24H,       // Last 24 hours data only
    LAST_SESSION    // Current app session only
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