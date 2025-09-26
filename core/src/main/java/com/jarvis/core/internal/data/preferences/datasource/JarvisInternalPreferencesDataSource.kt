@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.data.preferences.datasource

import androidx.annotation.RestrictTo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.jarvis.core.internal.data.preferences.JarvisInternalPreferencesKeys
import com.jarvis.core.internal.data.preferences.di.JarvisInternalDataStore
import com.jarvis.core.internal.data.preferences.model.JarvisInternalPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for managing internal SDK preferences
 * This handles the actual DataStore operations for internal SDK state.
 */
@Singleton
internal class JarvisInternalPreferencesDataSource @Inject constructor(
    @JarvisInternalDataStore private val dataStore: DataStore<Preferences>
) {

    /**
     * Get all internal preferences as a Flow
     */
    val preferences: Flow<JarvisInternalPreferences> = dataStore.data.map { preferences ->
        JarvisInternalPreferences(
            headerContentDismissed = preferences[JarvisInternalPreferencesKeys.HEADER_CONTENT_DISMISSED] ?: false,
            lastHeaderDismissalTimestamp = preferences[JarvisInternalPreferencesKeys.LAST_HEADER_DISMISSAL_TIMESTAMP] ?: 0L,
            sdkSessionId = preferences[JarvisInternalPreferencesKeys.SDK_SESSION_ID] ?: "",
            firstLaunchCompleted = preferences[JarvisInternalPreferencesKeys.FIRST_LAUNCH_COMPLETED] ?: false,
            debugModeEnabled = preferences[JarvisInternalPreferencesKeys.DEBUG_MODE_ENABLED] ?: false,
            lastPerformanceReportTimestamp = preferences[JarvisInternalPreferencesKeys.LAST_PERFORMANCE_REPORT_TIMESTAMP] ?: 0L
        )
    }

    /**
     * Set header content dismissed state
     */
    suspend fun setHeaderContentDismissed(dismissed: Boolean) {
        dataStore.edit { preferences ->
            preferences[JarvisInternalPreferencesKeys.HEADER_CONTENT_DISMISSED] = dismissed
            if (dismissed) {
                preferences[JarvisInternalPreferencesKeys.LAST_HEADER_DISMISSAL_TIMESTAMP] = System.currentTimeMillis()
            }
        }
    }

    /**
     * Set SDK session ID
     */
    suspend fun setSdkSessionId(sessionId: String) {
        dataStore.edit { preferences ->
            preferences[JarvisInternalPreferencesKeys.SDK_SESSION_ID] = sessionId
        }
    }

    /**
     * Set first launch completed
     */
    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[JarvisInternalPreferencesKeys.FIRST_LAUNCH_COMPLETED] = completed
        }
    }

    /**
     * Set debug mode enabled
     */
    suspend fun setDebugModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[JarvisInternalPreferencesKeys.DEBUG_MODE_ENABLED] = enabled
        }
    }

    /**
     * Update last performance report timestamp
     */
    suspend fun updateLastPerformanceReportTimestamp() {
        dataStore.edit { preferences ->
            preferences[JarvisInternalPreferencesKeys.LAST_PERFORMANCE_REPORT_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /**
     * Clear all internal preferences (for testing or reset)
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}