@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.domain.entity

import androidx.annotation.RestrictTo

/**
 * Enhanced preferences metrics with detailed analytics and chart data
 */
data class EnhancedPreferencesMetrics(
    // Basic metrics from existing PreferencesMetrics
    val totalPreferences: Int,
    val preferencesByType: Map<String, Int>,
    val mostCommonType: String?,
    val lastModified: Long?,
    
    // Enhanced analytics for charts
    val typeDistribution: List<PreferenceTypeData>,
    val sizeDistribution: List<PreferenceSizeData>,
    val activityOverTime: List<TimeSeriesDataPoint>,
    val storageUsage: StorageUsageData,
    
    // Session filtering
    val sessionFilter: SessionFilter,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Preference type distribution data for charts
 */
data class PreferenceTypeData(
    val type: String,                // SharedPreferences, DataStore, etc.
    val count: Int,
    val percentage: Float,
    val totalSize: Long,             // bytes
    val color: String = getTypeColor(type)
) {
    companion object {
        fun getTypeColor(type: String): String = when (type.lowercase()) {
            "sharedpreferences" -> "#4CAF50"
            "datastore" -> "#2196F3"
            "protodatastore" -> "#9C27B0"
            "room" -> "#FF9800"
            "encrypted" -> "#F44336"
            else -> "#607D8B"
        }
    }
}

/**
 * Preference size distribution for storage analysis
 */
data class PreferenceSizeData(
    val sizeRange: String,           // "< 1KB", "1-10KB", etc.
    val count: Int,
    val percentage: Float,
    val minSize: Long,
    val maxSize: Long
)

/**
 * Storage usage information
 */
data class StorageUsageData(
    val totalSize: Long,             // bytes
    val averageSize: Long,           // bytes per preference
    val largestPreference: PreferenceInfo?,
    val storageEfficiency: Float     // 0.0-100.0 efficiency score
)

/**
 * Individual preference information
 */
data class PreferenceInfo(
    val key: String,
    val type: String,
    val size: Long,
    val storageType: String,
    val lastModified: Long?
)

// Mock objects for testing and previews
object EnhancedPreferencesMetricsMock {
    val mockEnhancedPreferencesMetrics: EnhancedPreferencesMetrics
        get() = EnhancedPreferencesMetrics(
        totalPreferences = 42,
        preferencesByType = mapOf(
            "SHARED_PREFERENCES" to 25,
            "DATASTORE" to 12,
            "PROTO" to 5
        ),
        mostCommonType = "SHARED_PREFERENCES",
        lastModified = System.currentTimeMillis() - 3600000,
        typeDistribution = listOf(
            PreferenceTypeData("SharedPreferences", 25, 59.5f, 12800),
            PreferenceTypeData("DataStore", 12, 28.6f, 8400),
            PreferenceTypeData("ProtoDataStore", 5, 11.9f, 3200)
        ),
        sizeDistribution = listOf(
            PreferenceSizeData("< 1KB", 28, 66.7f, 0, 1024),
            PreferenceSizeData("1-10KB", 11, 26.2f, 1024, 10240),
            PreferenceSizeData("10KB+", 3, 7.1f, 10240, Long.MAX_VALUE)
        ),
        activityOverTime = listOf(
            TimeSeriesDataPoint(System.currentTimeMillis() - 86400000, 8f, "1d ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 43200000, 12f, "12h ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 21600000, 15f, "6h ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 10800000, 18f, "3h ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 3600000, 22f, "1h ago"),
            TimeSeriesDataPoint(System.currentTimeMillis(), 25f, "now")
        ),
        storageUsage = StorageUsageData(
            totalSize = 24400,
            averageSize = 580,
            largestPreference = PreferenceInfo(
                key = "user_profile_cache",
                type = "STRING",
                size = 5120,
                storageType = "SHARED_PREFERENCES",
                lastModified = System.currentTimeMillis() - 7200000
            ),
            storageEfficiency = 85.3f
        ),
        sessionFilter = SessionFilter.LAST_SESSION,
        lastUpdated = System.currentTimeMillis()
    )
}