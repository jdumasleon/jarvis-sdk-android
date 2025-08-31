package com.jarvis.features.settings.lib.api

/**
 * Public API for Jarvis Settings feature
 * Used by external modules to interact with Settings functionality
 */
interface JarvisSettings {
    
    /**
     * Clear all application data including network logs, preferences, and cache
     * @return Result indicating success or failure with error details
     */
    suspend fun clearAllData(): Result<Unit>
    
    /**
     * Get current application version information
     * @return AppInfo containing version details
     */
    suspend fun getAppInfo(): AppInfo
    
    /**
     * Get statistics about stored data
     * @return DataStats with counts and storage usage
     */
    suspend fun getDataStats(): DataStats
}

/**
 * Application information data class
 */
data class AppInfo(
    val appName: String,
    val version: String,
    val buildNumber: String,
    val packageName: String
)

/**
 * Data statistics for the application
 */
data class DataStats(
    val networkTransactions: Int,
    val preferences: Int, 
    val totalStorageBytes: Long
)