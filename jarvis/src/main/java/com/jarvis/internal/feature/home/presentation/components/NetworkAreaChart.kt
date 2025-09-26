@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.presentation.components

import androidx.annotation.RestrictTo

import android.content.res.Configuration
import java.util.Locale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.component.charts.DSAreaChart
import com.jarvis.core.internal.designsystem.component.charts.DSChartDataPoint
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.internal.feature.home.domain.entity.TimeSeriesDataPoint
import com.jarvis.library.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class for tooltip information
 */
data class TooltipData(
    val value: Float,
    val time: Long,
    val x: Float,
    val y: Float
)

/**
 * Data class for chart interaction state
 */
data class ChartInteractionState(
    val selectedXPosition: Float? = null,
    val selectedDataIndex: Int? = null,
    val isIndicatorVisible: Boolean = false
)

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
    // Track if animation has already been played to prevent re-animation
    var hasAnimated by remember { mutableStateOf(false) }
    
    // Only animate on first load
    val finalAnimationDuration = if (hasAnimated) 0 else animationDurationMs
    
    LaunchedEffect(dataPoints) {
        if (!hasAnimated && dataPoints.isNotEmpty()) {
            hasAnimated = true
        }
    }
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


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        // Header
        NetworkChartHeader(
            title = title,
            dataPoints = dataPoints,
            lineColor = DSJarvisTheme.colors.chart.primary
        )

        if (chartDataPoints.isNotEmpty()) {
            NetworkAreaChartContent(
                chartDataPoints = chartDataPoints,
                dataPoints = dataPoints,
                height = height,
                showGrid = showGrid,
                animationDurationMs = finalAnimationDuration,
                contentDesc = contentDesc
            )
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

@Composable
fun NetworkAreaChartContent(
    chartDataPoints: List<DSChartDataPoint>,
    dataPoints: List<TimeSeriesDataPoint>,
    height: Dp,
    showGrid: Boolean,
    animationDurationMs: Int,
    contentDesc: String
) {
    var tooltipData by remember { mutableStateOf<TooltipData?>(null) }
    var chartSize by remember { mutableStateOf(Size.Zero) }
    var interactionState by remember { mutableStateOf(ChartInteractionState()) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Animated indicator line position
    val indicatorXPosition = remember { Animatable(0f) }

    // Animation spec for smooth indicator movement
    val indicatorAnimationSpec: AnimationSpec<Float> = spring(
        dampingRatio = 0.8f,
        stiffness = 300f
    )
    
    // Calculate Y-axis values for the right side
    val yAxisValues = remember(dataPoints) {
        if (dataPoints.isNotEmpty()) {
            val maxValue = dataPoints.maxOf { it.value }
            val minValue = dataPoints.minOf { it.value }
            val range = maxValue - minValue
            listOf(
                maxValue,
                minValue + (range * 0.75f),
                minValue + (range * 0.5f),
                minValue + (range * 0.25f),
                minValue
            )
        } else {
            emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Chart area (takes most of the space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .onSizeChanged { size ->
                        chartSize = size.toSize()
                    }
                    .pointerInput(chartDataPoints) {
                        detectTapGestures { offset ->
                            if (dataPoints.isNotEmpty() && chartSize.width > 0) {
                                // Calculate which data point was touched
                                val tapX = offset.x
                                val chartWidth = chartSize.width

                                // Find the closest data point index
                                val normalizedX = (tapX / chartWidth).coerceIn(0f, 1f)
                                val index = (normalizedX * (dataPoints.size - 1)).roundToInt()
                                    .coerceIn(0, dataPoints.lastIndex)

                                if (index in dataPoints.indices) {
                                    // Update interaction state for indicator line
                                    interactionState = ChartInteractionState(
                                        selectedXPosition = tapX,
                                        selectedDataIndex = index,
                                        isIndicatorVisible = true
                                    )

                                    // Animate indicator line to the tapped position
                                    scope.launch {
                                        indicatorXPosition.animateTo(
                                            targetValue = tapX,
                                            animationSpec = indicatorAnimationSpec
                                        )
                                    }

                                    tooltipData = TooltipData(
                                        value = dataPoints[index].value,
                                        time = dataPoints[index].timestamp,
                                        x = offset.x,
                                        y = offset.y
                                    )

                                    // Auto-hide tooltip and indicator after 4 seconds
                                    scope.launch {
                                        delay(4000)
                                        tooltipData = null
                                        interactionState = interactionState.copy(isIndicatorVisible = false)
                                    }
                                }
                            }
                        }
                    }
            ) {
                DSAreaChart(
                    dataPoints = chartDataPoints,
                    modifier = Modifier.fillMaxSize(),
                    lineColor = DSJarvisTheme.colors.chart.primary,
                    fillStartColor = DSJarvisTheme.colors.chart.primary.copy(alpha = 0.3f),
                    fillEndColor = DSJarvisTheme.colors.chart.primary.copy(alpha = 0.05f),
                    backgroundColor = DSJarvisTheme.colors.extra.surface,
                    gridColor = DSJarvisTheme.colors.neutral.neutral20,
                    showGrid = showGrid,
                    animationDurationMs = animationDurationMs,
                    contentDescription = contentDesc
                )

                // Vertical indicator line overlay
                if (interactionState.isIndicatorVisible && interactionState.selectedDataIndex != null) {
                    val lineColor = DSJarvisTheme.colors.chart.primary
                    val backgroundColor = DSJarvisTheme.colors.extra.surface

                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawVerticalIndicator(
                            xPosition = indicatorXPosition.value,
                            chartSize = size,
                            dataPoints = dataPoints,
                            selectedIndex = interactionState.selectedDataIndex!!,
                            lineColor = lineColor,
                            pointColor = lineColor,
                            backgroundColor = backgroundColor
                        )
                    }
                }
            }
            
            // Y-axis numeric values on the right
            if (yAxisValues.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .height(height)
                        .padding(start = DSJarvisTheme.spacing.xs),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    yAxisValues.forEach { value ->
                        DSText(
                            text = value.roundToInt().toString(),
                            style = DSJarvisTheme.typography.label.small,
                            color = DSJarvisTheme.colors.neutral.neutral60,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Tooltip overlay
        tooltipData?.let { tooltip ->
            NetworkChartTooltip(
                tooltipData = tooltip,
                onDismiss = { tooltipData = null }
            )
        }
    }

    // Time axis labels
    if (dataPoints.isNotEmpty()) {
        NetworkTimeAxisLabels(
            dataPoints = dataPoints,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Tooltip component that shows detailed information about a data point
 */
@Composable
private fun NetworkChartTooltip(
    tooltipData: TooltipData,
    onDismiss: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    Popup(
        offset = IntOffset(
            x = tooltipData.x.roundToInt() - 60, // Center the tooltip
            y = tooltipData.y.roundToInt() - 80  // Show above the tap point
        ),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(8.dp))
                .clickable { onDismiss() },
            shape = RoundedCornerShape(8.dp),
            color = DSJarvisTheme.colors.extra.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(DSJarvisTheme.spacing.m),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                // Value
                DSText(
                    text = "${tooltipData.value.roundToInt()} requests",
                    style = DSJarvisTheme.typography.body.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.chart.primary
                )
                
                // Time
                DSText(
                    text = timeFormat.format(Date(tooltipData.time)),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                
                // Tap to dismiss hint
                DSText(
                    text = "Tap to dismiss",
                    style = DSJarvisTheme.typography.label.small,
                    color = DSJarvisTheme.colors.neutral.neutral40
                )
            }
        }
    }
}

/**
 * Draws the vertical indicator line and point on the chart
 */
private fun DrawScope.drawVerticalIndicator(
    xPosition: Float,
    chartSize: Size,
    dataPoints: List<TimeSeriesDataPoint>,
    selectedIndex: Int,
    lineColor: Color,
    pointColor: Color,
    backgroundColor: Color
) {
    if (selectedIndex !in dataPoints.indices || chartSize.width <= 0) return

    // Calculate the Y position of the selected point on the curve
    val minValue = dataPoints.minOf { it.value }
    val maxValue = dataPoints.maxOf { it.value }
    val valueRange = maxValue - minValue

    if (valueRange == 0f) return

    val selectedValue = dataPoints[selectedIndex].value
    val normalizedY = if (valueRange > 0f) {
        (selectedValue - minValue) / valueRange
    } else {
        0.5f
    }

    // Y position from bottom (inverted for screen coordinates)
    val yPosition = chartSize.height * (1f - normalizedY)

    // Draw vertical indicator line with dashed pattern
    val dashPattern = floatArrayOf(10f, 5f)
    val pathEffect = PathEffect.dashPathEffect(dashPattern, 0f)

    drawLine(
        color = lineColor.copy(alpha = 0.6f),
        start = Offset(xPosition, 0f),
        end = Offset(xPosition, chartSize.height),
        strokeWidth = 2.dp.toPx(),
        pathEffect = pathEffect
    )

    // Draw point indicator on the curve with white border
    val pointRadius = 6.dp.toPx()
    val borderRadius = pointRadius + 2.dp.toPx()

    // White border for better visibility
    drawCircle(
        color = backgroundColor,
        radius = borderRadius,
        center = Offset(xPosition, yPosition)
    )

    // Main point
    drawCircle(
        color = pointColor,
        radius = pointRadius,
        center = Offset(xPosition, yPosition)
    )

    // Inner highlight
    drawCircle(
        color = backgroundColor.copy(alpha = 0.3f),
        radius = pointRadius * 0.5f,
        center = Offset(xPosition, yPosition)
    )
}

/* ---------- Header & X labels ---------- */

/** Header with summary and a simple trend arrow. */
@Composable
fun NetworkChartHeader(
    title: String? = null,
    dataPoints: List<TimeSeriesDataPoint>,
    lineColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            title?.let {
                DSText(
                    text = it,
                    style = DSJarvisTheme.typography.title.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }

            if (dataPoints.isNotEmpty()) {
                val total = dataPoints.sumOf { it.value.toDouble() }.toInt()
                val avg = total / dataPoints.size
                DSText(
                    text = stringResource(R.string.total_requests, total),
                    style = DSJarvisTheme.typography.title.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                DSText(
                    text = stringResource(R.string.total_avg, avg),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral40
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
                    style = DSJarvisTheme.typography.label.small,
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
                style = DSJarvisTheme.typography.heading.medium,
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
                    value = String.format(Locale.US, "%.0fms", averageResponseTime),
                    color = DSJarvisTheme.colors.chart.secondary
                )
                NetworkMetricItem(
                    label = stringResource(R.string.error_rate),
                    value = String.format(Locale.US, "%.1f%%", errorRate),
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
            style = DSJarvisTheme.typography.title.large,
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
    uiMode = Configuration.UI_MODE_NIGHT_YES
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
    uiMode = Configuration.UI_MODE_NIGHT_YES
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