package com.jarvis.features.home.data.analyzer

import com.jarvis.features.home.domain.entity.*
import com.jarvis.features.preferences.domain.entity.AppPreference
import javax.inject.Inject
import kotlin.math.max

/**
 * Analyzer for preferences data to generate enhanced metrics and analytics
 */
class PreferencesAnalyzer @Inject constructor() {
    
    fun analyzePreferencesMetrics(
        preferences: List<AppPreference>,
        sessionFilter: SessionFilter
    ): EnhancedPreferencesMetrics {
        
        if (preferences.isEmpty()) {
            return getEmptyPreferencesMetrics(sessionFilter)
        }
        
        // Analyze preference types distribution
        val typeDistribution = analyzeTypeDistribution(preferences)
        
        // Analyze storage usage
        val storageUsage = analyzeStorageUsage(preferences)
        
        // Analyze size distribution
        val sizeDistribution = analyzeSizeDistribution(preferences)
        
        // Generate activity over time (dummy data for now since we don't have timestamps)
        val activityOverTime = generateActivityOverTime(preferences, sessionFilter)
        
        // Create basic metrics for compatibility
        val preferencesByType = preferences.groupBy { it.storageType.name }.mapValues { it.value.size }
        val mostCommonType = preferencesByType.maxByOrNull { it.value }?.key
        
        return EnhancedPreferencesMetrics(
            totalPreferences = preferences.size,
            preferencesByType = preferencesByType,
            mostCommonType = mostCommonType,
            lastModified = System.currentTimeMillis(),
            typeDistribution = typeDistribution,
            sizeDistribution = sizeDistribution,
            activityOverTime = activityOverTime,
            storageUsage = storageUsage,
            sessionFilter = sessionFilter,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun analyzeTypeDistribution(preferences: List<AppPreference>): List<PreferenceTypeData> {
        val typeGroups = preferences.groupBy { getPreferenceStorageType(it) }
        val totalPrefs = preferences.size.toFloat()
        
        // Define colors for different storage types
        val typeColors = mapOf(
            "SharedPreferences" to "#4CAF50",
            "DataStore" to "#2196F3", 
            "Database" to "#FF9800",
            "File Storage" to "#9C27B0",
            "Memory Cache" to "#607D8B",
            "Other" to "#9E9E9E"
        )
        
        return typeGroups.map { (type, typePreferences) ->
            val totalSize = typePreferences.sumOf { calculatePreferenceSize(it) }
            
            PreferenceTypeData(
                type = type,
                count = typePreferences.size,
                percentage = (typePreferences.size / totalPrefs) * 100f,
                totalSize = totalSize,
                color = typeColors[type] ?: "#9E9E9E"
            )
        }.sortedByDescending { it.count }
    }
    
    private fun analyzeStorageUsage(preferences: List<AppPreference>): StorageUsageData {
        if (preferences.isEmpty()) {
            return StorageUsageData(
                totalSize = 0L,
                averageSize = 0L,
                largestPreference = null,
                storageEfficiency = 100f
            )
        }
        
        val preferenceSizes = preferences.map { pref ->
            pref to calculatePreferenceSize(pref)
        }
        
        val totalSize = preferenceSizes.sumOf { it.second }
        val averageSize = totalSize / preferences.size
        
        val largestPreference = preferenceSizes.maxByOrNull { it.second }?.let { (pref, size) ->
            PreferenceInfo(
                key = pref.key,
                type = pref.type.name,
                size = size,
                storageType = pref.storageType.name,
                lastModified = System.currentTimeMillis()
            )
        }
        
        return StorageUsageData(
            totalSize = totalSize,
            averageSize = averageSize,
            largestPreference = largestPreference,
            storageEfficiency = calculateStorageEfficiency(preferences)
        )
    }
    
    private fun analyzeSizeDistribution(preferences: List<AppPreference>): List<PreferenceSizeData> {
        val sizeRanges = listOf(
            "0-100B" to (0L..100L),
            "100B-1KB" to (101L..1024L),
            "1-10KB" to (1025L..10240L),
            "10-100KB" to (10241L..102400L),
            "100KB+" to (102401L..Long.MAX_VALUE)
        )
        
        val totalPrefs = preferences.size.toFloat()
        
        return sizeRanges.map { (rangeName, range) ->
            val count = preferences.count { 
                val size = calculatePreferenceSize(it)
                size in range
            }
            
            PreferenceSizeData(
                sizeRange = rangeName,
                count = count,
                percentage = if (totalPrefs > 0) (count / totalPrefs) * 100f else 0f,
                minSize = range.first,
                maxSize = if (range.last == Long.MAX_VALUE) Long.MAX_VALUE else range.last
            )
        }.filter { it.count > 0 }
    }
    
    private fun generateActivityOverTime(
        preferences: List<AppPreference>,
        sessionFilter: SessionFilter
    ): List<TimeSeriesDataPoint> {
        // Generate synthetic activity data since we don't have real timestamps
        val currentTime = System.currentTimeMillis()
        val timePoints = when (sessionFilter) {
            SessionFilter.LAST_SESSION -> {
                // Generate hourly points for the last 12 hours
                (0..12).map { hour ->
                    TimeSeriesDataPoint(
                        timestamp = currentTime - (hour * 60 * 60 * 1000),
                        value = (preferences.size / 12f) * (0.8f + Math.random().toFloat() * 0.4f)
                    )
                }.reversed()
            }
            SessionFilter.GENERAL -> {
                // Generate daily points for the last 7 days
                (0..7).map { day ->
                    TimeSeriesDataPoint(
                        timestamp = currentTime - (day * 24 * 60 * 60 * 1000),
                        value = (preferences.size / 7f) * (0.7f + Math.random().toFloat() * 0.6f)
                    )
                }.reversed()
            }
        }
        
        return timePoints
    }
    
    private fun calculateStorageEfficiency(preferences: List<AppPreference>): Float {
        if (preferences.isEmpty()) return 100f
        
        val sizes = preferences.map { calculatePreferenceSize(it) }
        val averageSize = sizes.average()
        
        // Calculate efficiency based on size distribution and redundancy
        val sizeVariance = sizes.map { (it - averageSize) * (it - averageSize) }.average()
        val redundancyFactor = calculateRedundancyFactor(preferences)
        
        // Efficiency score: lower variance and redundancy = higher efficiency
        val varianceScore = if (averageSize > 0) {
            max(0f, 100f - (sizeVariance / averageSize * 100f).toFloat())
        } else 100f
        
        val redundancyScore = max(0f, 100f - redundancyFactor)
        
        return ((varianceScore + redundancyScore) / 2f).coerceIn(0f, 100f)
    }
    
    private fun calculateRedundancyFactor(preferences: List<AppPreference>): Float {
        // Calculate redundancy based on similar keys or duplicate patterns
        val keyPatterns = preferences.groupBy { extractKeyPattern(it.key) }
        val totalKeys = preferences.size.toFloat()
        
        var redundancyScore = 0f
        keyPatterns.forEach { (_, groupedPrefs) ->
            if (groupedPrefs.size > 1) {
                redundancyScore += (groupedPrefs.size - 1) / totalKeys * 100f
            }
        }
        
        return redundancyScore.coerceIn(0f, 100f)
    }
    
    private fun extractKeyPattern(key: String): String {
        // Extract pattern from key (e.g., "user_123" -> "user_*")
        return key.replace(Regex("\\d+"), "*")
                  .replace(Regex("[a-f0-9]{8,}"), "*") // Replace hex strings
    }
    
    private fun getPreferenceStorageType(preference: AppPreference): String {
        // Use the actual storage type from the preference
        return when (preference.storageType) {
            com.jarvis.features.preferences.domain.entity.PreferenceStorageType.SHARED_PREFERENCES -> "SharedPreferences"
            com.jarvis.features.preferences.domain.entity.PreferenceStorageType.PREFERENCES_DATASTORE -> "DataStore"
            com.jarvis.features.preferences.domain.entity.PreferenceStorageType.PROTO_DATASTORE -> "ProtoDataStore"
        }
    }
    
    private fun calculatePreferenceSize(preference: AppPreference): Long {
        // Calculate estimated size of preference
        val keySize = preference.key.toByteArray().size
        val valueSize = when (preference.value) {
            is String -> preference.value.toString().toByteArray().size
            is Int -> 4
            is Long -> 8
            is Float -> 4
            is Double -> 8
            is Boolean -> 1
            else -> preference.value.toString().toByteArray().size
        }
        
        return (keySize + valueSize).toLong()
    }
    
    private fun getEmptyPreferencesMetrics(sessionFilter: SessionFilter): EnhancedPreferencesMetrics {
        return EnhancedPreferencesMetrics(
            totalPreferences = 0,
            preferencesByType = emptyMap(),
            mostCommonType = null,
            lastModified = System.currentTimeMillis(),
            typeDistribution = emptyList(),
            sizeDistribution = emptyList(),
            activityOverTime = emptyList(),
            storageUsage = StorageUsageData(
                totalSize = 0L,
                averageSize = 0L,
                largestPreference = null,
                storageEfficiency = 100f
            ),
            sessionFilter = sessionFilter,
            lastUpdated = System.currentTimeMillis()
        )
    }
}