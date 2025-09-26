@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.domain.preferences.repository

import androidx.annotation.RestrictTo
import com.jarvis.core.internal.data.preferences.model.JarvisInternalPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing internal SDK preferences
 * This provides the contract for accessing and modifying internal SDK state.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface JarvisInternalPreferencesRepository {

    /**
     * Get all internal preferences as a Flow
     */
    val preferences: Flow<JarvisInternalPreferences>

    /**
     * Set header content dismissed state
     */
    suspend fun setHeaderContentDismissed(dismissed: Boolean)

    /**
     * Get header content dismissed state
     */
    suspend fun isHeaderContentDismissed(): Boolean

    /**
     * Set SDK session ID
     */
    suspend fun setSdkSessionId(sessionId: String)

    /**
     * Set first launch completed
     */
    suspend fun setFirstLaunchCompleted(completed: Boolean)

    /**
     * Set debug mode enabled
     */
    suspend fun setDebugModeEnabled(enabled: Boolean)

    /**
     * Update last performance report timestamp
     */
    suspend fun updateLastPerformanceReportTimestamp()

    /**
     * Clear all internal preferences
     */
    suspend fun clearAll()
}