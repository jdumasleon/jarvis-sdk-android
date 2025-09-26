@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.presentation.components

import androidx.annotation.RestrictTo

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.home.domain.entity.EndpointData
import com.jarvis.internal.feature.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.library.R
import java.util.Locale

/* ----------------------- Top Endpoints (main list) ----------------------- */

/**
 * Horizontal bar chart list of the top endpoints by request count.
 * - Sorts by requestCount desc
 * - Staggered bar animation
 * - Method color coding + quick metrics
 */
@Composable
fun TopEndpointsBarChartCard(
    title: String? = null,
    endpoints: List<EndpointData>,
    modifier: Modifier = Modifier,
    maxItems: Int = 10
) {

    DSCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Top endpoints by request count" }
            .testTag("TopEndpointsBarChart"),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        TopEndpointsBarChart(
            title = title,
            endpoints = endpoints,
            maxItems = maxItems
        )
    }
}

@Composable
fun TopEndpointsBarChart(
    modifier: Modifier = Modifier,
    title: String? = null,
    endpoints: List<EndpointData>,
    maxItems: Int = 5
) {
    // Defensive sorting so ranking and bar scaling are consistent
    val sorted = remember(endpoints) { endpoints.sortedByDescending { it.requestCount } }
    val topEndpoints = remember(sorted, maxItems) { sorted.take(maxItems) }
    val maxRequests = remember(sorted) { sorted.maxOfOrNull { it.requestCount } ?: 1 }

    var played by remember(sorted) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "bar_chart_animation"
    )
    LaunchedEffect(sorted) { played = true }

    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            title?.let {
                DSText(
                    text = stringResource(R.string.top_endpoints_title),
                    style = DSJarvisTheme.typography.title.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }

            DSText(
                text = stringResource(R.string.by_request_count),
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }

        if (topEndpoints.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                topEndpoints.forEachIndexed { index, endpoint ->
                    EndpointBarItem(
                        endpoint = endpoint,
                        maxRequests = maxRequests,
                        rank = index + 1,
                        // small delay cascade for each row
                        animationDelayMs = index * 70,
                        listProgress = progress
                    )
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                DSText(
                    text = stringResource(R.string.no_endpoint_data_available),
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

/**
 * Single row: method badge, endpoint path, right-aligned total, and an animated bar.
 */
@Composable
private fun EndpointBarItem(
    endpoint: EndpointData,
    maxRequests: Int,
    rank: Int,
    animationDelayMs: Int,
    listProgress: Float
) {
    // Each row animates based on overall list progress and a per-row delay.
    val target = (endpoint.requestCount.toFloat() / maxRequests) * listProgress
    val barProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 800, delayMillis = animationDelayMs),
        label = "bar_fill_animation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)) {
        // Header with rank, method, truncated path, and count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                // Rank badge (uses method hue at low alpha background)
                Box(
                    modifier = Modifier
                        .size(DSJarvisTheme.dimensions.l)
                        .clip(DSJarvisTheme.shapes.s)
                        .background(getMethodColor(endpoint.method).copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = rank.toString(),
                        style = DSJarvisTheme.typography.body.small,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(endpoint.method)
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

                // Endpoint (truncate gracefully)
                DSText(
                    text = endpoint.endpoint.let { if (it.length > 42) it.take(42) + "…" else it },
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium,
                    color = DSJarvisTheme.colors.neutral.neutral80,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            // Right-aligned count
            DSText(
                text = endpoint.requestCount.toString(),
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
        }

        // Bar + quick metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            // Animated progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(100))
                    .background(DSJarvisTheme.colors.neutral.neutral20)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barProgress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(100))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    getMethodColor(endpoint.method),
                                    getMethodColor(endpoint.method).copy(alpha = 0.85f)
                                )
                            )
                        )
                )
            }

            // Inline quick metrics (avg time & error rate)
            Row(horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
                MetricChip(
                    label = "${String.format(Locale.getDefault(), "%.0f", endpoint.averageResponseTime)}ms",
                    color = getResponseTimeColor(endpoint.averageResponseTime)
                )
                if (endpoint.errorRate > 0f) {
                    MetricChip(
                        label = "${String.format(Locale.getDefault(), "%.1f", endpoint.errorRate)}% err",
                        color = getErrorRateColor(endpoint.errorRate)
                    )
                }
            }
        }
    }
}

/* ----------------------- Tiny metric chip ----------------------- */

@Composable
private fun MetricChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.small,
            color = color
        )
    }
}

/* ----------------------- Method color helper ----------------------- */

private fun getMethodColor(method: String): Color = when (method.uppercase(Locale.getDefault())) {
    "GET" -> Color(0xFF4CAF50)  // Success green
    "POST" -> Color(0xFF2196F3) // Primary blue
    "PUT" -> Color(0xFFFF9800)  // Warning orange
    "DELETE" -> Color(0xFFF44336) // Error red
    "PATCH" -> Color(0xFF9C27B0)  // Purple
    "HEAD" -> Color(0xFF607D8B)   // Blue grey
    "OPTIONS" -> Color(0xFF795548) // Brown
    else -> Color(0xFF9E9E9E)     // Grey
}

@Composable
private fun getResponseTimeColor(responseTime: Float): Color = when {
    responseTime <= 500f -> DSJarvisTheme.colors.success.success100
    responseTime <= 1500f -> DSJarvisTheme.colors.warning.warning100
    else -> DSJarvisTheme.colors.error.error100
}

@Composable
private fun getErrorRateColor(errorRate: Float): Color = when {
    errorRate <= 1f -> DSJarvisTheme.colors.success.success100
    errorRate <= 3f -> DSJarvisTheme.colors.warning.warning100
    else -> DSJarvisTheme.colors.error.error100
}

/* ----------------------- Compact canvas chart ----------------------- */

/**
 * Compact horizontal bars (no text), great for small dashboard slots.
 */
@Composable
fun CompactEndpointsChart(
    endpoints: List<EndpointData>,
    modifier: Modifier = Modifier,
    height: Int = 120
) {
    val sorted = remember(endpoints) { endpoints.sortedByDescending { it.requestCount } }
    val top = remember(sorted) { sorted.take(5) }
    val maxRequests = remember(sorted) { sorted.maxOfOrNull { it.requestCount } ?: 1 }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .testTag("CompactEndpointsChart")
    ) {
        drawCompactBarChart(
            endpoints = top,
            maxRequests = maxRequests,
            canvasSize = size
        )
    }
}

/**
 * Draws the compact bars with rounded corners.
 */
private fun DrawScope.drawCompactBarChart(
    endpoints: List<EndpointData>,
    maxRequests: Int,
    canvasSize: Size
) {
    if (endpoints.isEmpty()) return

    val barHeight = 16.dp.toPx()
    val barSpacing = 8.dp.toPx()
    val padding = 16.dp.toPx()

    endpoints.forEachIndexed { index, endpoint ->
        val y = padding + index * (barHeight + barSpacing)
        val width = (endpoint.requestCount.toFloat() / maxRequests) * (canvasSize.width - 2 * padding)

        // Background track
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(padding, y),
            size = Size(canvasSize.width - 2 * padding, barHeight),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Foreground fill
        drawRoundRect(
            color = getMethodColor(endpoint.method),
            topLeft = Offset(padding, y),
            size = Size(width, barHeight),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
    }
}

/* ----------------------- Summary card ----------------------- */

@Composable
fun TopEndpointsCard(
    endpoints: List<EndpointData>,
    modifier: Modifier = Modifier
) {
    val total = remember(endpoints) { endpoints.sumOf { it.requestCount } }

    DSCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("TopEndpointsCard"),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Header with summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = stringResource(R.string.top_endpoints_title),
                    style = DSJarvisTheme.typography.heading.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                if (endpoints.isNotEmpty()) {
                    DSText(
                        text = stringResource(R.string.total_requests, total, 0),
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }

            TopEndpointsBarChart(
                endpoints = endpoints,
                maxItems = 8
            )
        }
    }
}


/* ----------------------- Previews (with sample data) ----------------------- */

@Preview(name = "Top Endpoints - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewTopEndpointsLight() {
    DSJarvisTheme {
        TopEndpointsBarChart(endpoints = mockEnhancedNetworkMetrics.topEndpoints, maxItems = 8)
    }
}

@Preview(
    name = "Top Endpoints - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewTopEndpointsDark() {
    DSJarvisTheme(darkTheme = true) {
        TopEndpointsBarChart(endpoints = mockEnhancedNetworkMetrics.topEndpoints, maxItems = 8)
    }
}

@Preview(name = "Top Endpoints - Empty", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewTopEndpointsEmpty() {
    DSJarvisTheme {
        TopEndpointsBarChart(endpoints = emptyList(), maxItems = 8)
    }
}

@Preview(name = "Top Endpoints Card - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewTopEndpointsCard() {
    DSJarvisTheme {
        TopEndpointsCard(endpoints = mockEnhancedNetworkMetrics.topEndpoints)
    }
}

@Preview(
    name = "Top Endpoints Card - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewTopEndpointsCardDark() {
    DSJarvisTheme(darkTheme = true) {
        TopEndpointsCard(endpoints = mockEnhancedNetworkMetrics.topEndpoints)
    }
}