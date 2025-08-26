package com.jarvis.features.home.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.charts.DSGaugeChart
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.HealthRating
import com.jarvis.features.home.domain.entity.HealthScore
import com.jarvis.features.home.domain.entity.HealthScoreMock.mockHealthScore
import com.jarvis.features.home.presentation.R

/**
 * Health score gauge component using the generic DSGaugeChart.
 * Shows health score with appropriate color coding and rating display.
 */
@Composable
fun HealthScoreGauge(
    healthScore: HealthScore,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 20.dp,
    indicatorCount: Int = 10,
    animationDurationMs: Int = 1000
) {
    val context = LocalContext.current
    val contentDesc = stringResource(
        R.string.health_score_accessibility,
        healthScore.overallScore.toInt(),
        healthScore.rating.displayName
    )

    // Create health-specific gradient
    val healthGradient = createHealthGradient(healthScore.overallScore)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        DSGaugeChart(
            value = healthScore.overallScore,
            maxValue = 100f,
            size = size,
            strokeWidth = strokeWidth,
            startAngle = 135f,
            sweepAngle = 270f,
            backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            foregroundColor = DSJarvisTheme.colors.chart.primary,
            gradient = healthGradient,
            indicatorCount = indicatorCount,
            showIndicators = true,
            animationDurationMs = animationDurationMs,
            contentDescription = contentDesc
        )

        // Score text overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DSText(
                text = "${healthScore.overallScore.toInt()}",
                style = DSJarvisTheme.typography.heading.large,
                fontWeight = FontWeight.Bold,
                color = getHealthScoreColor(healthScore.rating)
            )
            DSText(
                text = healthScore.rating.displayName,
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }
    }
}

/**
 * Creates a health-appropriate gradient based on score value
 */
@Composable
private fun createHealthGradient(score: Float): Brush {
    val colors = when {
        score >= 90f -> listOf(
            DSJarvisTheme.colors.success.success100,
            DSJarvisTheme.colors.success.success80
        )
        score >= 70f -> listOf(
            DSJarvisTheme.colors.success.success80,
            DSJarvisTheme.colors.warning.warning80
        )
        score >= 50f -> listOf(
            DSJarvisTheme.colors.warning.warning100,
            DSJarvisTheme.colors.warning.warning80
        )
        score >= 30f -> listOf(
            DSJarvisTheme.colors.warning.warning80,
            DSJarvisTheme.colors.error.error80
        )
        else -> listOf(
            DSJarvisTheme.colors.error.error100,
            DSJarvisTheme.colors.error.error80
        )
    }

    return Brush.sweepGradient(colors)
}

/**
 * Gets the appropriate color for health score based on rating
 */
@Composable
private fun getHealthScoreColor(rating: HealthRating): Color {
    return when (rating) {
        HealthRating.EXCELLENT -> DSJarvisTheme.colors.success.success100
        HealthRating.GOOD -> DSJarvisTheme.colors.success.success80
        HealthRating.AVERAGE -> DSJarvisTheme.colors.warning.warning100
        HealthRating.POOR -> DSJarvisTheme.colors.error.error80
        HealthRating.CRITICAL -> DSJarvisTheme.colors.error.error100
    }
}


/**
 * Health summary card with gauge and key metrics.
 */
@Composable
fun HealthSummaryCard(
    healthScore: HealthScore,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 180.dp
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        HealthSummary(
            healthScore = healthScore,
            modifier = Modifier.fillMaxWidth(),
            gaugeSize = gaugeSize
        )
    }
}

@Composable
fun HealthSummary(
    healthScore: HealthScore,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 180.dp
) {

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Health gauge
            HealthScoreGauge(
                healthScore = healthScore,
                modifier = Modifier.weight(1f),
                size = gaugeSize
            )

            // Key metrics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = DSJarvisTheme.spacing.l),
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                HealthMetricItem(
                    label = stringResource(R.string.requests),
                    value = "${healthScore.keyMetrics.totalRequests}",
                    color = DSJarvisTheme.colors.chart.primary
                )

                HealthMetricItem(
                    label = stringResource(R.string.error_rate),
                    value = String.format("%.1f%%", healthScore.keyMetrics.errorRate),
                    color = if (healthScore.keyMetrics.errorRate < 5f)
                        DSJarvisTheme.colors.success.success100
                    else DSJarvisTheme.colors.error.error100
                )

                HealthMetricItem(
                    label = stringResource(R.string.avg_response),
                    value = String.format("%.0fms", healthScore.keyMetrics.averageResponseTime),
                    color = DSJarvisTheme.colors.chart.secondary
                )

                HealthMetricItem(
                    label = stringResource(R.string.performance),
                    value = String.format("%.0f", healthScore.keyMetrics.performanceScore),
                    color = DSJarvisTheme.colors.chart.tertiary
                )
            }
        }
    }
}

/**
 * Individual health metric display item.
 */
@Composable
private fun HealthMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )

        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Preview(name = "HealthSummary - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewHealthSummaryCardLight() {
    DSJarvisTheme {
        HealthSummaryCard(
            healthScore = mockHealthScore.copy(rating = HealthRating.AVERAGE)
        )
    }
}

@Preview(name = "HealthSummary - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHealthSummaryCardDark() {
    DSJarvisTheme {
        HealthSummaryCard(
            healthScore = mockHealthScore.copy(rating = HealthRating.POOR)
        )
    }
}

@Preview(name = "HealthSummary - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewHealthSummaryDark() {
    DSJarvisTheme {
        HealthSummary(
            healthScore = mockHealthScore.copy(rating = HealthRating.POOR)
        )
    }
}

@Preview(name = "Gauge - Excellent", showBackground = true)
@Composable
private fun PreviewGaugeExcellent() {
    DSJarvisTheme {
        HealthScoreGauge(
            healthScore = mockHealthScore.copy(rating = HealthRating.EXCELLENT),
            size = 220.dp
        )
    }
}

@Preview(name = "Gauge - Poor", showBackground = true)
@Composable
private fun PreviewGaugePoor() {
    DSJarvisTheme {
        HealthScoreGauge(
            healthScore =mockHealthScore.copy(rating = HealthRating.CRITICAL),
            size = 220.dp
        )
    }
}