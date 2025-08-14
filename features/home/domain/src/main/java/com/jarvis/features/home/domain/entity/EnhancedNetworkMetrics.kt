package com.jarvis.features.home.domain.entity

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