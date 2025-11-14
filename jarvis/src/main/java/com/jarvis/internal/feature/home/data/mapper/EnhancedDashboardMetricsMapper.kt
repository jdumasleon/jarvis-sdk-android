@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.data.mapper

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.home.data.analyzer.PreferencesAnalyzer
import com.jarvis.internal.feature.home.domain.entity.EndpointData
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.EnhancedNetworkMetrics
import com.jarvis.internal.feature.home.domain.entity.HealthKeyMetrics
import com.jarvis.internal.feature.home.domain.entity.HealthRating
import com.jarvis.internal.feature.home.domain.entity.HealthScore
import com.jarvis.internal.feature.home.domain.entity.HealthScoreFactors
import com.jarvis.internal.feature.home.domain.entity.HttpMethodData
import com.jarvis.internal.feature.home.domain.entity.ResponseTimeDistribution
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import com.jarvis.internal.feature.home.domain.entity.SessionInfo
import com.jarvis.internal.feature.home.domain.entity.SlowEndpointData
import com.jarvis.internal.feature.home.domain.entity.TimeSeriesDataPoint
import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.preferences.internal.domain.entity.AppPreference
import kotlin.math.ceil
import javax.inject.Inject

/**
 * Mapper for enhanced dashboard metrics with advanced analytics
 */
class EnhancedDashboardMetricsMapper @Inject constructor(
    private val basicMapper: DashboardMetricsMapper,
    private val healthScoreCalculator: HealthScoreCalculator,
    private val networkAnalyzer: NetworkAnalyzer,
    private val preferencesAnalyzer: PreferencesAnalyzer
) {
    
    fun mapToEnhancedDashboardMetrics(
        networkTransactions: List<NetworkTransaction>,
        preferences: List<AppPreference>,
        sessionFilter: SessionFilter
    ): EnhancedDashboardMetrics {
        
        // Filter data based on session
        val filteredTransactions = when (sessionFilter) {
            SessionFilter.LAST_SESSION -> filterLastSession(networkTransactions)
            SessionFilter.LAST_24H -> filterLast24Hours(networkTransactions)
        }

        val filteredPreferences = when (sessionFilter) {
            SessionFilter.LAST_SESSION -> filterLastSessionPreferences(preferences)
            SessionFilter.LAST_24H -> preferences // Preferences are current state, include all
        }
        
        // Generate basic metrics for backward compatibility
        val basicMetrics = basicMapper.mapToDashboardMetrics(filteredTransactions, filteredPreferences)
        
        // Generate enhanced analytics
        val healthScore = healthScoreCalculator.calculateHealthScore(filteredTransactions, filteredPreferences)
        val enhancedNetworkMetrics = networkAnalyzer.analyzeNetworkMetrics(filteredTransactions, sessionFilter)
        val enhancedPreferencesMetrics = preferencesAnalyzer.analyzePreferencesMetrics(filteredPreferences, sessionFilter)
        
        // Create session info
        val sessionInfo = getCurrentSessionInfo(filteredTransactions)
        
        return EnhancedDashboardMetrics(
            networkMetrics = basicMetrics.networkMetrics,
            preferencesMetrics = basicMetrics.preferencesMetrics,
            performanceMetrics = basicMetrics.performanceMetrics,
            healthScore = healthScore,
            enhancedNetworkMetrics = enhancedNetworkMetrics,
            enhancedPreferencesMetrics = enhancedPreferencesMetrics,
            sessionInfo = sessionInfo,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun filterLastSession(transactions: List<NetworkTransaction>): List<NetworkTransaction> {
        if (transactions.isEmpty()) return emptyList()

        // Find the start of the current session (last app launch)
        // For now, we'll consider the last hour as the current session
        val sessionStartTime = System.currentTimeMillis() - (60 * 60 * 1000) // 1 hour ago

        return transactions.filter { it.startTime >= sessionStartTime }
    }

    private fun filterLast24Hours(transactions: List<NetworkTransaction>): List<NetworkTransaction> {
        if (transactions.isEmpty()) return emptyList()

        // Filter for last 24 hours to keep UI responsive and data manageable
        val last24HoursTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours ago

        return transactions.filter { it.startTime >= last24HoursTime }
    }

    private fun filterLastSessionPreferences(preferences: List<AppPreference>): List<AppPreference> {
        if (preferences.isEmpty()) return emptyList()

        // For preferences, we'll include all as they represent current state
        return preferences
    }
    
    private fun getCurrentSessionInfo(transactions: List<NetworkTransaction>): SessionInfo? {
        if (transactions.isEmpty()) return null
        
        val sessionStartTime = transactions.minOfOrNull { it.startTime } ?: System.currentTimeMillis()
        
        return SessionInfo(
            sessionId = "session_${sessionStartTime}",
            startTime = sessionStartTime,
            endTime = null, // Current session is ongoing
            isCurrentSession = true
        )
    }
}

/**
 * Health score calculator with weighted factors
 */
class HealthScoreCalculator @Inject constructor() {
    
    fun calculateHealthScore(
        transactions: List<NetworkTransaction>,
        preferences: List<AppPreference>
    ): HealthScore {
        
        if (transactions.isEmpty()) {
            return getDefaultHealthScore()
        }
        
        // Only count completed transactions (have a response)
        val completedTransactions = transactions.filter { it.response != null }

        // Successful: 2xx and 3xx (redirects are successful)
        val successfulTransactions = completedTransactions.filter {
            val status = it.response?.statusCode ?: 0
            status in 200..399
        }

        // Errors: Only 4xx (client errors) and 5xx (server errors)
        val errorTransactions = completedTransactions.filter {
            val status = it.response?.statusCode ?: 0
            status >= 400
        }

        // Calculate error rate only from completed transactions
        val errorRate = if (completedTransactions.isNotEmpty()) {
            (errorTransactions.size.toFloat() / completedTransactions.size) * 100f
        } else 0f
        
        val averageResponseTime = successfulTransactions
            .mapNotNull { it.duration }
            .average()
            .toFloat()
        
        // Calculate weighted score factors
        val factors = HealthScoreFactors(
            networkPerformance = calculateNetworkPerformanceScore(successfulTransactions),
            errorRate = calculateErrorRateScore(errorRate),
            responseTime = calculateResponseTimeScore(averageResponseTime),
            systemResources = calculateSystemResourcesScore(preferences)
        )
        
        // Calculate overall score with weights
        val overallScore = (
            factors.networkPerformance * 0.4f +  // 40% weight
            factors.errorRate * 0.3f +           // 30% weight  
            factors.responseTime * 0.2f +        // 20% weight
            factors.systemResources * 0.1f       // 10% weight
        ).coerceIn(0f, 100f)
        
        val rating = when {
            overallScore >= 90f -> HealthRating.EXCELLENT
            overallScore >= 75f -> HealthRating.GOOD
            overallScore >= 60f -> HealthRating.AVERAGE
            overallScore >= 40f -> HealthRating.POOR
            else -> HealthRating.CRITICAL
        }
        
        val keyMetrics = HealthKeyMetrics(
            totalRequests = completedTransactions.size, // Only count completed requests
            errorRate = errorRate,
            averageResponseTime = averageResponseTime,
            performanceScore = factors.networkPerformance,
            networkScore = factors.networkPerformance,
            uptime = if (errorRate < 5f) 99.9f else 95f + (5f - errorRate)
        )
        
        return HealthScore(
            overallScore = overallScore,
            rating = rating,
            keyMetrics = keyMetrics
        )
    }
    
    private fun calculateNetworkPerformanceScore(transactions: List<NetworkTransaction>): Float {
        if (transactions.isEmpty()) return 50f
        
        val responseTimes = transactions.mapNotNull { it.duration }
        if (responseTimes.isEmpty()) return 50f
        
        val averageTime = responseTimes.average()
        
        return when {
            averageTime < 200 -> 100f
            averageTime < 500 -> 90f
            averageTime < 1000 -> 75f
            averageTime < 2000 -> 60f
            averageTime < 5000 -> 40f
            else -> 20f
        }.toFloat()
    }
    
    private fun calculateErrorRateScore(errorRate: Float): Float {
        return when {
            errorRate == 0f -> 100f
            errorRate < 1f -> 95f
            errorRate < 3f -> 85f
            errorRate < 5f -> 70f
            errorRate < 10f -> 50f
            errorRate < 20f -> 30f
            else -> 10f
        }
    }
    
    private fun calculateResponseTimeScore(averageResponseTime: Float): Float {
        return when {
            averageResponseTime < 100 -> 100f
            averageResponseTime < 300 -> 90f
            averageResponseTime < 500 -> 80f
            averageResponseTime < 1000 -> 60f
            averageResponseTime < 2000 -> 40f
            else -> 20f
        }
    }
    
    private fun calculateSystemResourcesScore(preferences: List<AppPreference>): Float {
        // Simple scoring based on preference count and variety
        val score = when {
            preferences.size < 50 -> 100f
            preferences.size < 100 -> 90f
            preferences.size < 200 -> 80f
            preferences.size < 500 -> 60f
            else -> 40f
        }
        
        return score
    }
    
    private fun getDefaultHealthScore(): HealthScore {
        return HealthScore(
            overallScore = 85f,
            rating = HealthRating.GOOD,
            keyMetrics = HealthKeyMetrics(
                totalRequests = 0,
                errorRate = 0f,
                averageResponseTime = 0f,
                performanceScore = 85f,
                networkScore = 85f,
                uptime = 100f
            )
        )
    }
}

/**
 * Network data analyzer for enhanced metrics
 */
class NetworkAnalyzer @Inject constructor() {
    
    fun analyzeNetworkMetrics(
        transactions: List<NetworkTransaction>,
        sessionFilter: SessionFilter
    ): EnhancedNetworkMetrics {
        
        if (transactions.isEmpty()) {
            return getEmptyNetworkMetrics(sessionFilter)
        }
        
        // Generate time series data
        val requestsOverTime = generateRequestTimeSeriesData(transactions)
        
        // Analyze HTTP methods
        val httpMethodDistribution = analyzeHttpMethods(transactions)
        
        // Analyze endpoints
        val topEndpoints = analyzeTopEndpoints(transactions)
        val slowestEndpoints = analyzeSlowestEndpoints(transactions)
        
        // Status code distribution
        val statusCodeDistribution = transactions
            .mapNotNull { it.response?.statusCode }
            .groupingBy { it }
            .eachCount()
        
        // Response time distribution
        val responseTimeDistribution = analyzeResponseTimeDistribution(transactions)
        
        return EnhancedNetworkMetrics(
            totalCalls = transactions.size,
            averageSpeed = transactions.mapNotNull { it.duration }.average(),
            successfulCalls = transactions.count { it.response?.statusCode in 200..299 },
            failedCalls = transactions.count { it.response?.statusCode !in 200..299 },
            successRate = if (transactions.isNotEmpty()) {
                (transactions.count { it.response?.statusCode in 200..299 }
                    .toDouble() / transactions.size) * 100
            } else 100.0,
            averageRequestSize = calculateAverageRequestSize(transactions),
            averageResponseSize = calculateAverageResponseSize(transactions),
            requestsOverTime = requestsOverTime,
            httpMethodDistribution = httpMethodDistribution,
            topEndpoints = topEndpoints,
            slowestEndpoints = slowestEndpoints,
            statusCodeDistribution = statusCodeDistribution,
            responseTimeDistribution = responseTimeDistribution,
            sessionFilter = sessionFilter
        )
    }
    
    private fun generateRequestTimeSeriesData(transactions: List<NetworkTransaction>): List<TimeSeriesDataPoint> {
        if (transactions.isEmpty()) return emptyList()
        
        // Group by time intervals (e.g., per minute)
        val intervalMs = 60_000L // 1 minute intervals
        val startTime = transactions.minOf { it.startTime }
        val endTime = transactions.maxOf { it.startTime }
        val dataPoints = mutableListOf<TimeSeriesDataPoint>()
        var currentTime = startTime
        
        while (currentTime <= endTime) {
            val requestsInInterval = transactions.count { 
                it.startTime >= currentTime && it.startTime < currentTime + intervalMs 
            }
            
            dataPoints.add(
                TimeSeriesDataPoint(
                    timestamp = currentTime,
                    value = requestsInInterval.toFloat()
                )
            )
            
            currentTime += intervalMs
        }
        
        return if (dataPoints.size > MAX_TIME_SERIES_POINTS) {
            downsampleDataPoints(dataPoints, MAX_TIME_SERIES_POINTS)
        } else {
            dataPoints
        }
    }

    private fun downsampleDataPoints(
        points: List<TimeSeriesDataPoint>,
        targetCount: Int
    ): List<TimeSeriesDataPoint> {
        if (points.size <= targetCount) return points

        val bucketSize = ceil(points.size.toDouble() / targetCount.toDouble()).toInt().coerceAtLeast(1)

        return points.chunked(bucketSize).mapNotNull { bucket ->
            val firstPoint = bucket.firstOrNull() ?: return@mapNotNull null
            val totalValue = bucket.sumOf { it.value.toDouble() }.toFloat()

            TimeSeriesDataPoint(
                timestamp = firstPoint.timestamp,
                value = totalValue,
                label = firstPoint.label
            )
        }
    }
    
    private fun analyzeHttpMethods(transactions: List<NetworkTransaction>): List<HttpMethodData> {
        val methodGroups = transactions.groupBy { it.request.method.name }
        val totalRequests = transactions.size.toFloat()
        
        return methodGroups.map { (method, methodTransactions) ->
            val averageResponseTime = methodTransactions
                .mapNotNull { it.duration }
                .average()
                .toFloat()

            HttpMethodData(
                method = method,
                count = methodTransactions.size,
                percentage = (methodTransactions.size / totalRequests) * 100f,
                averageResponseTime = averageResponseTime
            )
        }.sortedByDescending { it.count }
    }
    
    private fun analyzeTopEndpoints(transactions: List<NetworkTransaction>): List<EndpointData> {
        val endpointGroups = transactions.groupBy { "${it.request.method.name} ${it.request.url}" }
        
        return endpointGroups.map { (endpoint, endpointTransactions) ->
            val averageResponseTime = endpointTransactions
                .mapNotNull { it.duration }
                .average()
                .toFloat()
            
            val errorRate = endpointTransactions
                .count { it.response?.statusCode !in 200..299 }
                .toFloat() / endpointTransactions.size * 100f
            
            val totalTraffic = calculateTotalTraffic(endpointTransactions)

            EndpointData(
                endpoint = endpoint,
                method = endpointTransactions.first().request.method.name,
                requestCount = endpointTransactions.size,
                averageResponseTime = averageResponseTime,
                errorRate = errorRate,
                totalTraffic = totalTraffic
            )
        }.sortedByDescending { it.requestCount }.take(10)
    }
    
    private fun analyzeSlowestEndpoints(transactions: List<NetworkTransaction>): List<SlowEndpointData> {
        val endpointGroups = transactions.groupBy { "${it.request.method.name} ${it.request.url}" }
        
        return endpointGroups.mapNotNull { (endpoint, endpointTransactions) ->
            val responseTimes = endpointTransactions.mapNotNull { it.duration }
            if (responseTimes.isEmpty()) return@mapNotNull null
            
            val averageResponseTime = responseTimes.average().toFloat()
            if (averageResponseTime < 500) return@mapNotNull null // Only include slow endpoints
            
            val p95ResponseTime = responseTimes.sorted().let { sorted ->
                val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
                sorted[index].toFloat()
            }
            
            val lastSlowRequest = endpointTransactions
                .filter { (it.duration ?: 0) > 1000 }
                .maxOfOrNull { it.startTime } ?: 0L

            SlowEndpointData(
                endpoint = endpoint,
                method = endpointTransactions.first().request.method.name,
                averageResponseTime = averageResponseTime,
                p95ResponseTime = p95ResponseTime,
                requestCount = endpointTransactions.size,
                lastSlowRequest = lastSlowRequest
            )
        }.sortedByDescending { it.averageResponseTime }.take(10)
    }
    
    private fun analyzeResponseTimeDistribution(transactions: List<NetworkTransaction>): ResponseTimeDistribution {
        val responseTimes = transactions.mapNotNull { it.duration }
        
        return ResponseTimeDistribution(
            under100ms = responseTimes.count { it < 100 },
            under500ms = responseTimes.count { it in 100..499 },
            under1s = responseTimes.count { it in 500..999 },
            under5s = responseTimes.count { it in 1000..4999 },
            over5s = responseTimes.count { it >= 5000 }
        )
    }
    
    private fun calculateAverageRequestSize(transactions: List<NetworkTransaction>): Long {
        return transactions
            .mapNotNull { it.request.body?.length?.toLong() }
            .average()
            .toLong()
    }
    
    private fun calculateAverageResponseSize(transactions: List<NetworkTransaction>): Long {
        return transactions
            .mapNotNull { it.response?.body?.length?.toLong() }
            .average()
            .toLong()
    }
    
    private fun calculateTotalTraffic(transactions: List<NetworkTransaction>): Long {
        val requestSize = transactions.sumOf { it.request.body?.length?.toLong() ?: 0L }
        val responseSize = transactions.sumOf { it.response?.body?.length?.toLong() ?: 0L }
        return requestSize + responseSize
    }
    
    private fun getEmptyNetworkMetrics(sessionFilter: SessionFilter): EnhancedNetworkMetrics {
        return EnhancedNetworkMetrics(
            totalCalls = 0,
            averageSpeed = 0.0,
            successfulCalls = 0,
            failedCalls = 0,
            successRate = 100.0,
            averageRequestSize = 0L,
            averageResponseSize = 0L,
            requestsOverTime = emptyList(),
            httpMethodDistribution = emptyList(),
            topEndpoints = emptyList(),
            slowestEndpoints = emptyList(),
            statusCodeDistribution = emptyMap(),
            responseTimeDistribution = ResponseTimeDistribution(0, 0, 0, 0, 0),
            sessionFilter = sessionFilter
        )
    }

    private companion object {
        private const val MAX_TIME_SERIES_POINTS = 20
    }
}
