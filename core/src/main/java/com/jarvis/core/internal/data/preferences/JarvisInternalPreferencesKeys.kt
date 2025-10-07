@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.data.preferences

import androidx.annotation.RestrictTo
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Internal SDK DataStore preference keys
 * These keys are used for SDK internal state management and should not be exposed externally.
 */
object JarvisInternalPreferencesKeys {

    // Header content preferences
    val HEADER_CONTENT_DISMISSED = booleanPreferencesKey("header_content_dismissed")
    val LAST_HEADER_DISMISSAL_TIMESTAMP = longPreferencesKey("last_header_dismissal_timestamp")

    // SDK session preferences
    val SDK_SESSION_ID = stringPreferencesKey("sdk_session_id")
    val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")

    // Debug and performance preferences
    val DEBUG_MODE_ENABLED = booleanPreferencesKey("debug_mode_enabled")
    val LAST_PERFORMANCE_REPORT_TIMESTAMP = longPreferencesKey("last_performance_report_timestamp")
}