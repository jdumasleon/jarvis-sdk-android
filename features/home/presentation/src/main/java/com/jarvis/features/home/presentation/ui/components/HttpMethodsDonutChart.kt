package com.jarvis.features.home.presentation.ui.components

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
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.charts.DSDonutChart
import com.jarvis.core.designsystem.component.charts.DSDonutChartData
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EnhancedNetworkMetricsMock.mockEnhancedNetworkMetrics
import com.jarvis.features.home.domain.entity.HttpMethodData
import com.jarvis.features.home.presentation.R

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
    // Convert HttpMethodData to DSDonutChartData
    val chartData = remember(httpMethods) {
        val normalized = normalizePercentages(httpMethods)
        normalized.map { method ->
            DSDonutChartData(
                value = method.count.toFloat(),
                label = method.method,
                color = safeParseColor(method.color)
            )
        }
    }
    
    val totalRequests = remember(chartData) { chartData.sumOf { it.value.toInt() } }
    
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
            animationDurationMs = animationDurationMs,
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
                    text = String.format("%.1f%%", methodData.percentage),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }

        // Average response time
        DSText(
            text = String.format("%.0fms", methodData.averageResponseTime),
            style = DSJarvisTheme.typography.body.small,
            color = safeParseColor(methodData.color)
        )
    }
}


/**
 * Expanded HTTP methods card with additional metrics
 */
@Composable
fun HttpMethodsCard(
    httpMethods: List<HttpMethodData>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 200.dp
) {
    // Ordena por número de peticiones desc para la lista y leyenda
    val sorted = remember(httpMethods) { httpMethods.sortedByDescending { it.count } }
    val dataKey = remember(sorted) { sorted.hashCode() }
    var played by remember(dataKey) { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (played) 1f else 0f,
        animationSpec = tween(900),
        label = "methods_card_anim"
    )
    LaunchedEffect(dataKey) { played = true }

    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Header with summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = stringResource(R.string.http_methods_distribution),
                    style = DSJarvisTheme.typography.heading.large,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                val total = sorted.sumOf { it.count }
                DSText(
                    text = stringResource(R.string.total_requests, total, 0),
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }

            // Donut chart + leyenda (top 6)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HttpMethodsDonutChart(
                    httpMethods = sorted,
                    size = chartSize,
                    showCenterTotal = true
                )
                Spacer(Modifier.width(DSJarvisTheme.spacing.m))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    sorted.take(6).forEach { method ->
                        HttpMethodLegendItem(
                            methodData = method,
                            animationProgress = progress
                        )
                    }
                }
            }

            // Detailed list
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
        }
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
                text = String.format("%.1f%%", methodData.percentage),
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            DSText(
                text = String.format("%.0fms", methodData.averageResponseTime),
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
    val closeTo100 = kotlin.math.abs(sumPct - 100f) <= 1f
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
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
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
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
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