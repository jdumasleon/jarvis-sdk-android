package com.jarvis.features.home.domain.entity

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