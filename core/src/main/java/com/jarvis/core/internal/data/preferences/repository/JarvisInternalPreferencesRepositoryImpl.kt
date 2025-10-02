@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.data.preferences.repository

import androidx.annotation.RestrictTo
import com.jarvis.core.internal.data.preferences.datasource.JarvisInternalPreferencesDataSource
import com.jarvis.core.internal.data.preferences.model.JarvisInternalPreferences
import com.jarvis.core.internal.domain.preferences.repository.JarvisInternalPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of JarvisInternalPreferencesRepository
 * This handles the business logic for internal SDK preferences management.
 */
@Singleton
class JarvisInternalPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: JarvisInternalPreferencesDataSource
) : JarvisInternalPreferencesRepository {

    override val preferences: Flow<JarvisInternalPreferences> = dataSource.preferences

    override suspend fun setHeaderContentDismissed(dismissed: Boolean) {
        dataSource.setHeaderContentDismissed(dismissed)
    }

    override suspend fun isHeaderContentDismissed(): Boolean {
        return dataSource.preferences.first().headerContentDismissed
    }

    override suspend fun setSdkSessionId(sessionId: String) {
        dataSource.setSdkSessionId(sessionId)
    }

    override suspend fun setFirstLaunchCompleted(completed: Boolean) {
        dataSource.setFirstLaunchCompleted(completed)
    }

    override suspend fun setDebugModeEnabled(enabled: Boolean) {
        dataSource.setDebugModeEnabled(enabled)
    }

    override suspend fun updateLastPerformanceReportTimestamp() {
        dataSource.updateLastPerformanceReportTimestamp()
    }

    override suspend fun clearAll() {
        dataSource.clearAll()
    }
}