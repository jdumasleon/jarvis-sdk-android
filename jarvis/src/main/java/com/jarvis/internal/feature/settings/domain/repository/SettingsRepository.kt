package com.jarvis.internal.feature.settings.domain.repository

import com.jarvis.internal.feature.settings.domain.entity.AppInfo
import com.jarvis.internal.feature.settings.domain.entity.SettingsAppInfo
import com.jarvis.internal.feature.settings.domain.entity.SdkInfo
import com.jarvis.internal.feature.settings.domain.entity.HostAppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Settings functionality
 */
interface SettingsRepository {

    /**
     * Get application information (version, build, etc.)
     * @deprecated Use getSettingsAppInfo() instead for clearer separation
     */
    suspend fun getAppInfo(): Flow<Result<AppInfo>>

    /**
     * Get SDK information
     */
    suspend fun getSdkInfo(): Flow<Result<SdkInfo>>

    /**
     * Get host application information
     */
    suspend fun getHostAppInfo(): Flow<Result<HostAppInfo>>

    /**
     * Get combined settings app information
     */
    suspend fun getSettingsAppInfo(): Flow<Result<SettingsAppInfo>>

}