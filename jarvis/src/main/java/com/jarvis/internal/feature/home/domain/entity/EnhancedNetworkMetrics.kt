package com.jarvis.internal.feature.home.domain.entity

/**
 * Enhanced network metrics with detailed analytics and chart data
 */
data class EnhancedNetworkMetrics(
    // Basic metrics from existing NetworkMetrics
    val totalCalls: Int,
    val averageSpeed: Double,
    val successfulCalls: Int,
    val failedCalls: Int,
    val successRate: Double,
    val averageRequestSize: Long,
    val averageResponseSize: Long,
    
    // Enhanced analytics for charts
    val requestsOverTime: List<TimeSeriesDataPoint>,
    val httpMethodDistribution: List<HttpMethodData>,
    val topEndpoints: List<EndpointData>,
    val slowestEndpoints: List<SlowEndpointData>,
    val statusCodeDistribution: Map<Int, Int>,
    val responseTimeDistribution: ResponseTimeDistribution,
    
    // Session filtering
    val sessionFilter: SessionFilter,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Time series data point for requests over time chart
 */
data class TimeSeriesDataPoint(
    val timestamp: Long,
    val value: Float,
    val label: String? = null
)

/**
 * HTTP method distribution data for donut chart
 */
data class HttpMethodData(
    val method: String,              // GET, POST, PUT, DELETE, etc.
    val count: Int,
    val percentage: Float,
    val averageResponseTime: Float,
    val color: String = getMethodColor(method)
) {
    companion object {
        fun getMethodColor(method: String): String = when (method.uppercase()) {
            "GET" -> "#4CAF50"
            "POST" -> "#2196F3"
            "PUT" -> "#FF9800"
            "DELETE" -> "#F44336"
            "PATCH" -> "#9C27B0"
            else -> "#607D8B"
        }
    }
}

/**
 * Endpoint performance data for bar chart
 */
data class EndpointData(
    val endpoint: String,
    val method: String,
    val requestCount: Int,
    val averageResponseTime: Float,
    val errorRate: Float,
    val totalTraffic: Long           // bytes
)

/**
 * Slowest endpoint data for performance insights
 */
data class SlowEndpointData(
    val endpoint: String,
    val method: String,
    val averageResponseTime: Float,
    val p95ResponseTime: Float,
    val requestCount: Int,
    val lastSlowRequest: Long        // timestamp
)

/**
 * Response time distribution for performance analysis
 */
data class ResponseTimeDistribution(
    val under100ms: Int,
    val under500ms: Int,
    val under1s: Int,
    val under5s: Int,
    val over5s: Int
) {
    val total: Int
        get() = under100ms + under500ms + under1s + under5s + over5s
        
    val percentages: ResponseTimePercentages
        get() = ResponseTimePercentages(
            under100ms = if (total > 0) (under100ms.toFloat() / total * 100) else 0f,
            under500ms = if (total > 0) (under500ms.toFloat() / total * 100) else 0f,
            under1s = if (total > 0) (under1s.toFloat() / total * 100) else 0f,
            under5s = if (total > 0) (under5s.toFloat() / total * 100) else 0f,
            over5s = if (total > 0) (over5s.toFloat() / total * 100) else 0f
        )
}

/**
 * Response time percentages for display
 */
data class ResponseTimePercentages(
    val under100ms: Float,
    val under500ms: Float,
    val under1s: Float,
    val under5s: Float,
    val over5s: Float
)

// Mock objects for testing and previews
object EnhancedNetworkMetricsMock {
    val mockEnhancedNetworkMetrics: EnhancedNetworkMetrics
        get() = EnhancedNetworkMetrics(
        totalCalls = 247,
        averageSpeed = 156.8,
        successfulCalls = 231,
        failedCalls = 16,
        successRate = 93.5,
        averageRequestSize = 2048,
        averageResponseSize = 4096,
        requestsOverTime = listOf(
            TimeSeriesDataPoint(System.currentTimeMillis() - 3600000, 45f, "1h ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 1800000, 67f, "30m ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 900000, 52f, "15m ago"),
            TimeSeriesDataPoint(System.currentTimeMillis() - 300000, 73f, "5m ago"),
            TimeSeriesDataPoint(System.currentTimeMillis(), 82f, "now")
        ),
        httpMethodDistribution = listOf(
            HttpMethodData("GET", 156, 63.2f, 120.5f),
            HttpMethodData("POST", 62, 25.1f, 245.3f),
            HttpMethodData("PUT", 18, 7.3f, 189.7f),
            HttpMethodData("DELETE", 11, 4.4f, 98.2f)
        ),
        topEndpoints = listOf(
            EndpointData("GET /api/users", "GET", 89, 125.3f, 2.1f, 512000),
            EndpointData("POST /api/auth", "POST", 45, 234.7f, 0.8f, 256000),
            EndpointData("GET /api/dashboard", "GET", 38, 156.2f, 5.2f, 384000),
            EndpointData("PUT /api/profile", "PUT", 23, 189.5f, 3.1f, 128000)
        ),
        slowestEndpoints = listOf(
            SlowEndpointData("GET /api/reports", "GET", 2850.3f, 3200.1f, 12, System.currentTimeMillis() - 180000),
            SlowEndpointData("POST /api/upload", "POST", 1834.7f, 2100.4f, 8, System.currentTimeMillis() - 300000),
            SlowEndpointData("GET /api/analytics", "GET", 1245.2f, 1500.8f, 15, System.currentTimeMillis() - 120000)
        ),
        statusCodeDistribution = mapOf(
            200 to 189,
            201 to 34,
            400 to 8,
            401 to 3,
            404 to 6,
            500 to 7
        ),
        responseTimeDistribution = ResponseTimeDistribution(
            under100ms = 89,
            under500ms = 124,
            under1s = 18,
            under5s = 12,
            over5s = 4
        ),
        sessionFilter = SessionFilter.LAST_SESSION,
        lastUpdated = System.currentTimeMillis()
    )
}