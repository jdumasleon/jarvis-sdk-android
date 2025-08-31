package com.jarvis.features.settings.domain.repository

import com.jarvis.features.settings.domain.entity.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Settings functionality
 */
interface SettingsRepository {
    
    /**
     * Get application information (version, build, etc.)
     */
    suspend fun getAppInfo(): Flow<Result<AppInfo>>

}