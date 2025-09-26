package com.jarvis.core.internal.data.preferences.model

import androidx.annotation.RestrictTo

/**
 * Internal SDK preferences data model
 * These preferences are used internally by the Jarvis SDK and should not be exposed
 * to external apps or shown in the preferences feature.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class JarvisInternalPreferences(
    val headerContentDismissed: Boolean = false,
    val lastHeaderDismissalTimestamp: Long = 0L,
    val sdkSessionId: String = "",
    val firstLaunchCompleted: Boolean = false,
    val debugModeEnabled: Boolean = false,
    val lastPerformanceReportTimestamp: Long = 0L
)