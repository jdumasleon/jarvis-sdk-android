package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.features.home.domain.entity.SlowEndpointData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Slowest endpoints list to spot performance bottlenecks.
 * - Sorts by averageResponseTime (desc)
 * - Animated severity color, counters, and bars
 * - Suggestion banner for very slow endpoints
 */
@Composable
fun SlowestEndpointsListCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    slowEndpoints: List<SlowEndpointData>,
    maxItems: Int = 8,
    maxHeight: Dp = 400.dp
) {
    DSCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Slowest endpoints list" }
            .testTag("SlowestEndpointsList"),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        SlowestEndpointsList(
            title = title,
            slowEndpoints = slowEndpoints,
            maxItems = maxItems,
            maxHeight = maxHeight
        )
    }
}

@Composable
fun SlowestEndpointsList(
    modifier: Modifier = Modifier,
    title: String? = null,
    slowEndpoints: List<SlowEndpointData>,
    maxItems: Int = 8,
    maxHeight: Dp = 400.dp
) {
    // Sort once to ensure consistent ranking and visual scale
    val sorted = remember(slowEndpoints) { slowEndpoints.sortedByDescending { it.averageResponseTime } }
    val topSlowEndpoints = remember(sorted, maxItems) { sorted.take(maxItems) }
    val worstAvg = remember(sorted) { sorted.maxOfOrNull { it.averageResponseTime } ?: 1f }

    var animationPlayed by remember(sorted) { mutableStateOf(false) }
    LaunchedEffect(sorted) { animationPlayed = true }

    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        // Header with warning indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {

                title?.let {
                    DSText(
                        modifier = modifier.weight(1f),
                        text = it,
                        style = DSJarvisTheme.typography.title.large,
                        fontWeight = FontWeight.Bold,
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )
                }
            }
        }

        if (topSlowEndpoints.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs),
                modifier = Modifier.heightIn(max = maxHeight)
            ) {
                itemsIndexed(topSlowEndpoints) { index, endpoint ->
                    SlowEndpointItem(
                        endpoint = endpoint,
                        animationPlayed = animationPlayed,
                        rank = index + 1,
                        worstAvg = worstAvg
                    )
                }
            }
        } else {
            // Empty state - great performance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSText(
                        text = "🎉",
                        style = DSJarvisTheme.typography.heading.small
                    )
                    DSText(
                        text = "No slow endpoints detected",
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50)
                    )
                    DSText(
                        text = "All endpoints are performing well",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }
    }
}

/**
 * Single slow endpoint row with rank, method, timings, proportional bar, and optional suggestion.
 */
@Composable
private fun SlowEndpointItem(
    endpoint: SlowEndpointData,
    animationPlayed: Boolean,
    rank: Int,
    worstAvg: Float
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = rank * 80),
        label = "slow_endpoint_animation"
    )

    val severityColor = getSeverityColor(endpoint.averageResponseTime)
    val animatedSeverityColor by animateColorAsState(
        targetValue = if (animationPlayed) severityColor else DSJarvisTheme.colors.neutral.neutral40,
        animationSpec = tween(durationMillis = 800, delayMillis = rank * 40),
        label = "severity_color_animation"
    )

    val animatedResponseTime by animateFloatAsState(
        targetValue = if (animationPlayed) endpoint.averageResponseTime else 0f,
        animationSpec = tween(durationMillis = 900, delayMillis = rank * 60),
        label = "response_time_animation"
    )

    // Relative width for the severity bar (bounded to [0.08, 1] for visibility)
    val barRatio = (endpoint.averageResponseTime / worstAvg).coerceIn(0.08f, 1f)
    val animatedBarRatio by animateFloatAsState(
        targetValue = if (animationPlayed) barRatio else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = rank * 50),
        label = "bar_ratio_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DSJarvisTheme.shapes.m)
            .padding(DSJarvisTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        // Header: rank badge, method badge, response time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(DSJarvisTheme.dimensions.l)
                        .clip(DSJarvisTheme.shapes.s)
                        .background(animatedSeverityColor),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "$rank",
                        style = DSJarvisTheme.typography.body.medium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Method badge
                Box(
                    modifier = Modifier
                        .clip(DSJarvisTheme.shapes.s)
                        .background(getMethodColor(endpoint.method))
                        .padding(horizontal = DSJarvisTheme.spacing.s, vertical = DSJarvisTheme.spacing.xs)
                ) {
                    DSText(
                        text = endpoint.method.uppercase(Locale.getDefault()),
                        style = DSJarvisTheme.typography.body.small,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            DSText(
                text = "${String.format(Locale.getDefault(), "%.0f", animatedResponseTime)}ms",
                style = DSJarvisTheme.typography.title.large,
                fontWeight = FontWeight.Bold,
                color = animatedSeverityColor
            )
        }

        // Endpoint path (truncate gracefully in parent container)
        DSText(
            text = endpoint.endpoint,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )

        // Proportional severity bar (full width background + filled foreground)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(100))
                .background(DSJarvisTheme.colors.neutral.neutral20)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedBarRatio)
                    .clip(RoundedCornerShape(100))
                    .background(animatedSeverityColor.copy(alpha = 0.6f))
            )
        }

        // Metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)) {
                PerformanceMetricChip(
                    label = "P95",
                    value = "${String.format(Locale.getDefault(), "%.0f", endpoint.p95ResponseTime)}ms",
                    color = animatedSeverityColor
                )
                PerformanceMetricChip(
                    label = "Requests",
                    value = "${endpoint.requestCount}",
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }

            // Last slow time (HH:mm) if available
            if (endpoint.lastSlowRequest > 0) {
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                DSText(
                    text = "Last: ${timeFormat.format(Date(endpoint.lastSlowRequest))}",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }

        // Suggestion banner for very slow endpoints
        if (endpoint.averageResponseTime > 2000) {
            PerformanceSuggestion(
                responseTime = endpoint.averageResponseTime,
                requestCount = endpoint.requestCount
            )
        }
    }
}

/**
 * Small two-line metric used inside a row.
 */
@Composable
private fun PerformanceMetricChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            color = color
        )
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/**
 * Suggestion banner explaining potential next steps for slow endpoints.
 */
@Composable
private fun PerformanceSuggestion(
    responseTime: Float,
    requestCount: Int
) {
    val suggestion = when {
        responseTime > 5000 -> "Critical: consider caching, DB indexes, or query decomposition."
        responseTime > 2000 && requestCount > 100 -> "High traffic: consider load balancing or async offloading."
        responseTime > 1000 -> "Slow response: review N+1 queries and heavy joins."
        else -> "Monitor for performance degradation."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF3E0))
            .padding(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = "💡 $suggestion",
            style = DSJarvisTheme.typography.body.small,
            color = Color(0xFFE65100)
        )
    }
}

/* ---------------------- Color helpers ---------------------- */

/**
 * Severity color based on average response time.
 */
private fun getSeverityColor(responseTime: Float): Color = when {
    responseTime > 5000 -> Color(0xFFF44336)  // Critical - Red
    responseTime > 2000 -> Color(0xFFFF9800)  // High - Orange
    responseTime > 1000 -> Color(0xFFFFC107)  // Medium - Yellow
    responseTime > 500  -> Color(0xFF2196F3)  // Low - Blue
    else -> Color(0xFF4CAF50)                 // Good - Green
}

/**
 * HTTP method color chip.
 */
private fun getMethodColor(method: String): Color = when (method.uppercase(Locale.getDefault())) {
    "GET" -> Color(0xFF4CAF50)
    "POST" -> Color(0xFF2196F3)
    "PUT" -> Color(0xFFFF9800)
    "DELETE" -> Color(0xFFF44336)
    "PATCH" -> Color(0xFF9C27B0)
    "HEAD" -> Color(0xFF607D8B)
    "OPTIONS" -> Color(0xFF795548)
    else -> Color(0xFF9E9E9E)
}

/* ---------------------- Compact summary (optional) ---------------------- */

/**
 * Compact dashboard card summarizing slow endpoints.
 */
@Composable
fun SlowestEndpointsSummary(
    slowEndpoints: List<SlowEndpointData>,
    modifier: Modifier = Modifier
) {
    val count = slowEndpoints.size
    DSCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("SlowestEndpointsSummary"),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Performance Issues",
                    style = DSJarvisTheme.typography.heading.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )

                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        DSText(
                            text = "$count slow",
                            style = DSJarvisTheme.typography.body.small,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            // Inline top 5 list
            SlowestEndpointsList(
                slowEndpoints = slowEndpoints,
                maxItems = 5
            )
        }
    }
}

/* ---------------------- Local DSCard shim ---------------------- */

@Composable
private fun DSCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = DSJarvisTheme.shapes.m,
    elevation: Dp = DSJarvisTheme.elevations.level1,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = shape,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = elevation),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = DSJarvisTheme.colors.extra.background
        ),
        content = { content() }
    )
}

/* ---------------------- Previews with sample data ---------------------- */

@Preview(name = "Slowest Endpoints - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewSlowestEndpointsDark() {
    DSJarvisTheme {
        SlowestEndpointsList(
            slowEndpoints = mockEnhancedNetworkMetrics.slowestEndpoints,
            maxItems = 8
        )
    }
}

@Preview(name = "Slowest Endpoints - Empty", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewSlowestEndpointsEmpty() {
    DSJarvisTheme {
        SlowestEndpointsList(
            slowEndpoints = emptyList(),
            maxItems = 8
        )
    }
}

@Preview(name = "Summary Card", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewSlowestEndpointsSummary() {
    DSJarvisTheme {
        SlowestEndpointsSummary(
            slowEndpoints = mockEnhancedNetworkMetrics.slowestEndpoints.take(5)
        )
    }
}