@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.presentation.components

import androidx.annotation.RestrictTo

import android.content.res.Configuration
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.component.charts.DSDonutChart
import com.jarvis.core.internal.designsystem.component.charts.DSDonutChartData
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.internal.feature.home.domain.entity.HttpMethodData
import com.jarvis.library.R
import kotlin.math.abs

/**
 * HTTP methods distribution donut chart using the generic DSDonutChart.
 * Shows HTTP method distribution with center total.
 */
@Composable
fun HttpMethodsDonutChart(
    httpMethods: List<HttpMethodData>,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 24.dp,
    animationDurationMs: Int = 1200,
    showCenterTotal: Boolean = true
) {
    // Optimize: Create stable key based on data content, not hashCode
    val dataKey = remember(httpMethods) {
        httpMethods.joinToString("|") { "${it.method}:${it.count}" }
    }

    // Optimize: Track if animation has already been played
    var hasAnimated by remember(dataKey) { mutableStateOf(false) }
    val finalAnimationDuration = if (hasAnimated) 0 else animationDurationMs

    LaunchedEffect(dataKey) {
        if (!hasAnimated) {
            hasAnimated = true
        }
    }

    // Optimize: Use derivedStateOf for expensive data conversion
    val chartData by remember(httpMethods) {
        derivedStateOf {
            val normalized = normalizePercentages(httpMethods)
            normalized.map { method ->
                DSDonutChartData(
                    value = method.count.toFloat(),
                    label = method.method,
                    color = safeParseColor(method.color)
                )
            }
        }
    }

    val totalRequests by remember(chartData) {
        derivedStateOf { chartData.sumOf { it.value.toInt() } }
    }
    
    val contentDesc = stringResource(
        R.string.donut_chart_accessibility,
        chartData.size,
        totalRequests.toString()
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        DSDonutChart(
            data = chartData,
            size = size,
            strokeWidth = strokeWidth,
            colors = DSJarvisTheme.colors.chart.colors,
            backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            animationDurationMs = finalAnimationDuration,  // Use optimized duration
            contentDescription = contentDesc
        )

        if (showCenterTotal && chartData.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSText(
                    text = "$totalRequests",
                    style = DSJarvisTheme.typography.heading.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                DSText(
                    text = stringResource(R.string.requests),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

/**
 * Legend item for HTTP method with color indicator and stats
 */
@Composable
private fun HttpMethodLegendItem(
    methodData: HttpMethodData,
    animationProgress: Float
) {
    val target = methodData.count.toFloat()
    val animatedCount by animateFloatAsState(
        targetValue = if (animationProgress > 0f) target else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "method_count_animation"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(safeParseColor(methodData.color))
        )

        // Method name and stats
        Column(modifier = Modifier.weight(1f)) {
            DSText(
                text = methodData.method,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral80
            )

            Row(horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)) {
                DSText(
                    text = "${animatedCount.toInt()}",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                DSText(
                    text = "•",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                DSText(
                    text = String.format(Locale.US, "%.1f%%", methodData.percentage),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }

        // Average response time
        DSText(
            text = String.format(Locale.US, "%.0fms", methodData.averageResponseTime),
            style = DSJarvisTheme.typography.body.small,
            color = safeParseColor(methodData.color)
        )
    }
}


/**
 * Expanded HTTP methods card with additional metrics
 */
@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun HttpMethodsCard(
    title: String? = null,
    httpMethods: List<HttpMethodData>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        HttpMethodsWithDetails(
            title = title,
            httpMethods = httpMethods,
            chartSize = chartSize
        )
    }
}

// Optimize: Break down large composable to reduce compiler memory
@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun HttpMethodsWithDetails(
    title: String? = null,
    httpMethods: List<HttpMethodData>,
    chartSize: Dp = 180.dp,
    inline: Boolean = false
) {
    // Optimize: Use derivedStateOf for expensive sorting operation
    val sorted by remember(httpMethods) {
        derivedStateOf { httpMethods.sortedByDescending { it.count } }
    }

    // Optimize: Create stable key based on data content, not hashCode
    val dataKey = remember(sorted) {
        sorted.joinToString("|") { "${it.method}:${it.count}" }
    }

    var played by remember(dataKey) { mutableStateOf(false) }

    // Optimize: Skip animation entirely if already played
    val progress by if (!played) {
        animateFloatAsState(
            targetValue = if (played) 1f else 0f,
            animationSpec = tween(900),
            label = "methods_card_anim"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    LaunchedEffect(dataKey) {
        if (!played) {
            played = true
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)) {
        HttpMethodsHeader(title, sorted)
        HttpMethodsChartSection(sorted, chartSize, inline, progress)
        if (!inline) {
            HttpMethodsDetailedList(sorted, chartSize)
        }
    }
}

// Optimize: Extract header
@Composable
private fun HttpMethodsHeader(title: String?, sorted: List<HttpMethodData>) {
    title?.let {
        DSText(
            text = stringResource(R.string.http_methods_distribution),
            style = DSJarvisTheme.typography.heading.large,
            fontWeight = FontWeight.Bold,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
    val total = sorted.sumOf { it.count }
    DSText(
        text = stringResource(R.string.total_requests, total, 0),
        style = DSJarvisTheme.typography.body.medium,
        color = DSJarvisTheme.colors.neutral.neutral60
    )
}

// Optimize: Extract chart section
@Composable
private fun HttpMethodsChartSection(
    sorted: List<HttpMethodData>,
    chartSize: Dp,
    inline: Boolean,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HttpMethodsDonutChart(
            modifier = Modifier.weight(1f),
            httpMethods = sorted,
            size = chartSize,
            showCenterTotal = true
        )
        Spacer(Modifier.width(DSJarvisTheme.spacing.m))
        if (inline) {
            HttpMethodsLegend(sorted, progress)
        }
    }
}

// Optimize: Extract legend
@Composable
private fun RowScope.HttpMethodsLegend(sorted: List<HttpMethodData>, progress: Float) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        sorted.forEach { method ->
            HttpMethodLegendItem(
                methodData = method,
                animationProgress = progress
            )
        }
    }
}

// Optimize: Extract detailed list
@Composable
private fun HttpMethodsDetailedList(sorted: List<HttpMethodData>, chartSize: Dp) {
    if (sorted.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
            DSText(
                text = stringResource(R.string.http_methods_distribution),
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral80
            )

            sorted.forEach { method ->
                HttpMethodDetailItem(methodData = method)
            }
        }
    } else {
        HttpMethodsEmptyState(chartSize)
    }
}

// Optimize: Extract empty state
@Composable
private fun HttpMethodsEmptyState(chartSize: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartSize),
        contentAlignment = Alignment.Center
    ) {
        DSText(
            text = stringResource(R.string.no_network_data_available),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/**
 * Detailed item showing HTTP method information
 */
@Composable
private fun HttpMethodDetailItem(
    methodData: HttpMethodData
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DSJarvisTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(safeParseColor(methodData.color))
            )
            DSText(
                text = methodData.method,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral80
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)) {
            DSText(
                text = "${methodData.count}",
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            DSText(
                text = String.format(Locale.US, "%.1f%%", methodData.percentage),
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            DSText(
                text = String.format(Locale.US, "%.0fms", methodData.averageResponseTime),
                style = DSJarvisTheme.typography.body.medium,
                color = safeParseColor(methodData.color)
            )
        }
    }
}

private fun safeParseColor(hex: String?): Color = runCatching {
    if (hex.isNullOrBlank()) Color(0xFF90CAF9) // fallback azul suave
    else Color(android.graphics.Color.parseColor(hex))
}.getOrElse { Color(0xFF90CAF9) }

/**
 * Si los porcentajes no suman ~100, recalculamos a partir de los counts.
 * Si suman ~100 (tolerancia 1%), respetamos el porcentaje de entrada.
 */
private fun normalizePercentages(input: List<HttpMethodData>): List<HttpMethodData> {
    if (input.isEmpty()) return input
    val sumPct = input.sumOf { it.percentage.toDouble() }.toFloat()
    val closeTo100 = abs(sumPct - 100f) <= 1f
    return if (closeTo100) input
    else {
        val total = input.sumOf { it.count }.toFloat().takeIf { it > 0f } ?: 1f
        input.map { it.copy(percentage = (it.count / total) * 100f) }
    }
}


@Preview(name = "Donut - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewHttpMethodsDonutLight() {
    DSJarvisTheme {
        HttpMethodsDonutChart(httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution)
    }
}

@Preview(name = "Donut Card - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewHttpMethodsDonutCardLight() {
    DSJarvisTheme {
        HttpMethodsCard(
            httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution,
            chartSize = 180.dp
        )
    }
}

@Preview(
    name = "Donut - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewHttpMethodsDonutDark() {
    DSJarvisTheme(darkTheme = true) {
        HttpMethodsDonutChart(httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution)
    }
}

@Preview(
    name = "Donut Card - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewHttpMethodsDonutCardDark() {
    DSJarvisTheme(darkTheme = true) {
        HttpMethodsCard(
            httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution,
            chartSize = 180.dp
        )
    }
}

@Preview(
    name = "Theme Comparison - Side by Side",
    showBackground = true,
    widthDp = 800,
    heightDp = 400
)
@Composable
private fun PreviewThemeComparison() {
    Row(modifier = Modifier.fillMaxSize()) {
        // Light theme
        DSJarvisTheme(darkTheme = false) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(DSJarvisTheme.colors.extra.background)
                    .padding(16.dp)
            ) {
                DSText(
                    text = "LIGHT THEME",
                    style = DSJarvisTheme.typography.title.large,
                    color = DSJarvisTheme.colors.neutral.neutral100,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HttpMethodsCard(
                    httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution.take(3),
                    chartSize = 120.dp
                )
            }
        }
        
        // Dark theme
        DSJarvisTheme(darkTheme = true) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(DSJarvisTheme.colors.extra.background)
                    .padding(16.dp)
            ) {
                DSText(
                    text = "DARK THEME",
                    style = DSJarvisTheme.typography.title.large,
                    color = DSJarvisTheme.colors.neutral.neutral100,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HttpMethodsCard(
                    httpMethods = mockEnhancedNetworkMetrics.httpMethodDistribution.take(3),
                    chartSize = 120.dp
                )
            }
        }
    }
}