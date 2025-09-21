package com.jarvis.internal.feature.home.data.mapper

import com.jarvis.internal.feature.home.domain.entity.DashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.NetworkMetrics
import com.jarvis.internal.feature.home.domain.entity.PerformanceMetrics
import com.jarvis.internal.feature.home.domain.entity.PerformanceRating
import com.jarvis.internal.feature.home.domain.entity.PreferencesMetrics
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.preferences.domain.entity.AppPreference
import javax.inject.Inject

/**
 * Mapper to convert raw data into dashboard metrics
 */
class DashboardMetricsMapper @Inject constructor() {

    // ---- Public API ----

    fun mapToDashboardMetrics(
        networkTransactions: List<NetworkTransaction>,
        preferences: List<AppPreference>
    ): DashboardMetrics {
        val networkMetrics = mapNetworkMetrics(networkTransactions)
        val preferencesMetrics = mapPreferencesMetrics(preferences)
        val performanceMetrics = mapPerformanceMetrics(networkTransactions)

        return DashboardMetrics(
            networkMetrics = networkMetrics,
            preferencesMetrics = preferencesMetrics,
            performanceMetrics = performanceMetrics
        )
    }

    // ---- Internals ----

    private fun mapNetworkMetrics(transactions: List<NetworkTransaction>): NetworkMetrics {
        if (transactions.isEmpty()) {
            return NetworkMetrics(
                totalCalls = 0,
                averageSpeed = 0.0,
                successfulCalls = 0,
                failedCalls = 0,
                successRate = 0.0,
                averageRequestSize = 0L,
                averageResponseSize = 0L,
                mostUsedEndpoint = null,
                p50 = 0.0, p90 = 0.0, p95 = 0.0, p99 = 0.0,
                topSlowEndpoints = emptyList()
            )
        }

        // Durations in ms
        val durations = transactions.asSequence()
            .mapNotNull { it.duration?.toDouble() }
            .toList()

        val p = percentiles(durations, 50, 90, 95, 99)

        val successfulCalls = transactions.count { it.response?.isSuccessful == true }
        val failedCalls = transactions.size - successfulCalls
        val successRate = successfulCalls.toDouble() / transactions.size * 100.0

        val averageSpeed = durations.average().takeIf { !it.isNaN() } ?: 0.0

        val averageRequestSize = transactions.asSequence()
            .map { it.request.bodySize } // Long
            .average().toLong()

        val averageResponseSize = transactions.asSequence()
            .mapNotNull { it.response?.bodySize?.toDouble() }
            .average().let { if (it.isNaN()) 0L else it.toLong() }

        val mostUsedEndpoint = transactions.asSequence()
            .groupingBy { "${it.request.method} ${normalizePath(it.request.url)}" }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        // Top endpoints más lentos por p95 (evita que pocos outliers distorsionen)
        val topSlowEndpoints = transactions
            .groupBy { "${it.request.method} ${normalizePath(it.request.url)}" }
            .mapValues { (_, list) ->
                val ds = list.mapNotNull { it.duration?.toDouble() }
                percentiles(ds, 95)[95] ?: 0.0
            }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        return NetworkMetrics(
            totalCalls = transactions.size,
            averageSpeed = averageSpeed,
            successfulCalls = successfulCalls,
            failedCalls = failedCalls,
            successRate = successRate,
            averageRequestSize = averageRequestSize,
            averageResponseSize = averageResponseSize,
            mostUsedEndpoint = mostUsedEndpoint,
            p50 = p[50] ?: 0.0,
            p90 = p[90] ?: 0.0,
            p95 = p[95] ?: 0.0,
            p99 = p[99] ?: 0.0,
            topSlowEndpoints = topSlowEndpoints
        )
    }

    private fun mapPreferencesMetrics(preferences: List<AppPreference>): PreferencesMetrics {
        val preferencesByType = preferences.groupingBy { it.storageType.toString() }.eachCount()
        val mostCommonType = preferencesByType.maxByOrNull { it.value }?.key
        // AppPreference no tiene lastModified real; mantenemos fallback por compatibilidad
        val lastModified = if (preferences.isNotEmpty()) System.currentTimeMillis() else null

        return PreferencesMetrics(
            totalPreferences = preferences.size,
            preferencesByType = preferencesByType,
            mostCommonType = mostCommonType,
            lastModified = lastModified
        )
    }

    private fun mapPerformanceMetrics(transactions: List<NetworkTransaction>): PerformanceMetrics {
        if (transactions.isEmpty()) {
            return PerformanceMetrics(
                overallRating = PerformanceRating.EXCELLENT,
                averageResponseTime = 0.0,
                slowestCall = null,
                fastestCall = null,
                errorRate = 0.0,
                p95 = 0.0,
                apdex = 1.0
            )
        }

        val durations = transactions.mapNotNull { it.duration?.toDouble() }
        val averageResponseTime = durations.average().takeIf { !it.isNaN() } ?: 0.0
        val slowestCall = durations.maxOrNull()
        val fastestCall = durations.minOrNull()

        val errorRate = transactions.count { it.response?.isSuccessful != true }
            .toDouble() / transactions.size * 100.0

        val p95 = percentiles(durations, 95)[95] ?: 0.0
        val apdexT = 1000.0 // ms (ajústalo a tu SLA)
        val apdexScore = apdex(durations, apdexT)

        val overallRating = when {
            errorRate > 10 -> PerformanceRating.CRITICAL
            apdexScore < 0.70 -> PerformanceRating.POOR
            apdexScore < 0.85 -> PerformanceRating.AVERAGE
            apdexScore < 0.94 -> PerformanceRating.GOOD
            else -> PerformanceRating.EXCELLENT
        }

        return PerformanceMetrics(
            overallRating = overallRating,
            averageResponseTime = averageResponseTime,
            slowestCall = slowestCall,
            fastestCall = fastestCall,
            errorRate = errorRate,
            p95 = p95,
            apdex = apdexScore
        )
    }

    // ---- Helpers ----

    /**
     * Linear-interpolated percentiles. Returns a map p -> value.
     */
    private fun percentiles(valuesMs: List<Double>, vararg ps: Int): Map<Int, Double> {
        if (valuesMs.isEmpty()) return ps.associateWith { 0.0 }
        val sorted = valuesMs.sorted()
        return ps.associateWith { p ->
            val r = (p / 100.0) * (sorted.size - 1)
            val i = r.toInt()
            val f = r - i
            if (i + 1 < sorted.size) sorted[i] * (1 - f) + sorted[i + 1] * f else sorted.last()
        }
    }

    /**
     * Apdex score given a threshold T (ms): 1.0 best, 0.0 worst.
     */
    private fun apdex(latenciesMs: List<Double>, T: Double): Double {
        if (latenciesMs.isEmpty()) return 1.0
        val satisfied = latenciesMs.count { it <= T }
        val tolerated = latenciesMs.count { it in (T + 0.0001)..(4 * T) }
        return (satisfied + tolerated / 2.0) / latenciesMs.size
    }

    /**
     * Normalize path removing IDs/UUIDs to avoid fragmentation in grouping.
     */
    private val idRegex = Regex("/\\d+|/[0-9a-fA-F-]{8,}")
    private fun normalizePath(url: String): String =
        url.substringBefore("?").replace(idRegex, "/{id}")
}