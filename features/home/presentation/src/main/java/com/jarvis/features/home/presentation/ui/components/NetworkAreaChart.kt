package com.jarvis.features.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.charts.DSAreaChart
import com.jarvis.core.designsystem.component.charts.DSChartDataPoint
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.features.home.domain.entity.TimeSeriesDataPoint
import com.jarvis.features.home.presentation.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Network area chart component using the generic DSAreaChart.
 * Shows requests over time with interaction and tooltip.
 */
@Composable
fun NetworkAreaChart(
    dataPoints: List<TimeSeriesDataPoint>,
    modifier: Modifier = Modifier,
    title: String? = null,
    height: Dp = 200.dp,
    showGrid: Boolean = true,
    showAverageLine: Boolean = true,
    enableInteraction: Boolean = true,
    animationDurationMs: Int = 1200
) {
    // Convert TimeSeriesDataPoint to DSChartDataPoint
    val chartDataPoints = remember(dataPoints) {
        dataPoints.sortedBy { it.timestamp }.mapIndexed { index, point ->
            DSChartDataPoint(
                x = index.toFloat(),
                y = point.value,
                label = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(point.timestamp))
            )
        }
    }

    val chartTitle = title ?: stringResource(R.string.requests_over_time)
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val contentDesc = if (dataPoints.isNotEmpty()) {
        val last = dataPoints.last()
        stringResource(
            R.string.network_area_chart_accessibility,
            last.value.toInt(),
            timeFormat.format(Date(last.timestamp))
        )
    } else {
        stringResource(R.string.no_network_data_available)
    }

    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Header
            NetworkChartHeader(
                title = chartTitle,
                dataPoints = dataPoints,
                lineColor = DSJarvisTheme.colors.chart.primary
            )

            if (chartDataPoints.isNotEmpty()) {
                DSAreaChart(
                    dataPoints = chartDataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    lineColor = DSJarvisTheme.colors.chart.primary,
                    fillStartColor = DSJarvisTheme.colors.chart.primary.copy(alpha = 0.3f),
                    fillEndColor = DSJarvisTheme.colors.chart.primary.copy(alpha = 0.05f),
                    backgroundColor = DSJarvisTheme.colors.extra.surface,
                    gridColor = DSJarvisTheme.colors.neutral.neutral20,
                    showGrid = showGrid,
                    animationDurationMs = animationDurationMs,
                    contentDescription = contentDesc
                )

                // Time axis labels
                if (dataPoints.isNotEmpty()) {
                    NetworkTimeAxisLabels(
                        dataPoints = dataPoints,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = stringResource(R.string.no_network_data_available),
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }
    }
}

/* ---------- Header & X labels ---------- */

/** Header with summary and a simple trend arrow. */
@Composable
private fun NetworkChartHeader(
    title: String,
    dataPoints: List<TimeSeriesDataPoint>,
    lineColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            DSText(
                text = title,
                style = DSJarvisTheme.typography.heading.heading5,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            if (dataPoints.isNotEmpty()) {
                val total = dataPoints.sumOf { it.value.toDouble() }.toInt()
                val avg = total / dataPoints.size
                DSText(
                    text = stringResource(R.string.total_requests, total, avg),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }

        if (dataPoints.size >= 2) {
            val last = dataPoints.last()
            val prev = dataPoints[dataPoints.lastIndex - 1]
            val up = last.value > prev.value
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                DSText(
                    text = if (up) "↗" else "↘",
                    style = DSJarvisTheme.typography.body.medium,
                    color = if (up) DSJarvisTheme.colors.success.success100 else DSJarvisTheme.colors.error.error100
                )
                DSText(
                    text = last.value.toInt().toString(),
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium,
                    color = lineColor
                )
            }
        }
    }
}

/** Time axis labels (first / middle / last). */
@Composable
private fun NetworkTimeAxisLabels(
    dataPoints: List<TimeSeriesDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
        val indices = listOf(0, dataPoints.size / 2, dataPoints.lastIndex).distinct()
        indices.forEach { idx ->
            if (idx in dataPoints.indices) {
                DSText(
                    text = timeFormat.format(Date(dataPoints[idx].timestamp)),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

/* ---------- Optional overview card ---------- */

@Composable
fun NetworkOverviewCard(
    dataPoints: List<TimeSeriesDataPoint>,
    totalRequests: Int,
    averageResponseTime: Float,
    errorRate: Float,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 150.dp
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = stringResource(R.string.network_overview),
                style = DSJarvisTheme.typography.heading.heading4,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetworkMetricItem(
                    label = stringResource(R.string.requests),
                    value = totalRequests.toString(),
                    color = DSJarvisTheme.colors.chart.primary
                )
                NetworkMetricItem(
                    label = stringResource(R.string.avg_time),
                    value = String.format("%.0fms", averageResponseTime),
                    color = DSJarvisTheme.colors.chart.secondary
                )
                NetworkMetricItem(
                    label = stringResource(R.string.error_rate),
                    value = String.format("%.1f%%", errorRate),
                    color = if (errorRate < 5f) DSJarvisTheme.colors.success.success100 else DSJarvisTheme.colors.error.error100
                )
            }

            NetworkAreaChart(
                dataPoints = dataPoints,
                title = stringResource(R.string.request_timeline),
                height = chartHeight
            )
        }
    }
}

@Composable
private fun NetworkMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DSText(
            text = value,
            style = DSJarvisTheme.typography.heading.heading5,
            fontWeight = FontWeight.Bold,
            color = color
        )
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/* ---------- Previews ---------- */

@Preview(name = "AreaChart - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNetworkAreaChartLight() {
    DSJarvisTheme {
        NetworkAreaChart(
            dataPoints = mockEnhancedNetworkMetrics.requestsOverTime,
            title = "Requests Over Time",
            showAverageLine = true,
            showGrid = true,
            enableInteraction = true
        )
    }
}

@Preview(
    name = "AreaChart - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewNetworkAreaChartDark() {
    DSJarvisTheme(darkTheme = true) {
        NetworkAreaChart(
            dataPoints = mockEnhancedNetworkMetrics.requestsOverTime,
            title = "Requests Over Time",
            showAverageLine = true,
            showGrid = true,
            enableInteraction = true
        )
    }
}

@Preview(
    name = "Network Overview - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewNetworkOverviewCardDark() {
    DSJarvisTheme(darkTheme = true) {
        NetworkOverviewCard(
            dataPoints = mockEnhancedNetworkMetrics.requestsOverTime,
            totalRequests = 15420,
            averageResponseTime = 245.6f,
            errorRate = 2.3f
        )
    }
}