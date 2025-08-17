package com.jarvis.core.designsystem.component.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generic gauge chart component with customizable colors and styling
 */
@Composable
fun DSGaugeChart(
    value: Float,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 20.dp,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    backgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    foregroundColor: Color = DSJarvisTheme.colors.chart.primary,
    gradient: Brush? = null,
    indicatorCount: Int = 10,
    showIndicators: Boolean = true,
    animationDurationMs: Int = 1000,
    contentDescription: String? = null
) {
    val normalizedValue = (value / maxValue).coerceIn(0f, 1f)
    
    val animatedValue by animateFloatAsState(
        targetValue = normalizedValue,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "gauge_animation"
    )

    val accessibilityDesc = contentDescription ?: "Gauge showing $value out of $maxValue"

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = accessibilityDesc }
            .testTag("DSGaugeChart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGauge(
                value = animatedValue,
                strokeWidthPx = strokeWidth.toPx(),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                backgroundColor = backgroundColor,
                foregroundColor = foregroundColor,
                gradient = gradient,
                indicatorCount = if (showIndicators) indicatorCount else 0
            )
        }
    }
}

private fun DrawScope.drawGauge(
    value: Float,
    strokeWidthPx: Float,
    startAngle: Float,
    sweepAngle: Float,
    backgroundColor: Color,
    foregroundColor: Color,
    gradient: Brush?,
    indicatorCount: Int
) {
    val radius = (size.minDimension - strokeWidthPx) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)

    // Background arc
    drawArc(
        color = backgroundColor,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )

    // Foreground arc
    val valueSweepAngle = value * sweepAngle
    val brush = gradient ?: Brush.linearGradient(listOf(foregroundColor, foregroundColor))
    
    drawArc(
        brush = brush,
        startAngle = startAngle,
        sweepAngle = valueSweepAngle,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )

    // Indicators
    if (indicatorCount > 0) {
        drawIndicators(
            center = center,
            outerRadius = radius + strokeWidthPx / 2f + 10.dp.toPx(),
            count = indicatorCount,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            foregroundColor = foregroundColor
        )
    }
}

private fun DrawScope.drawIndicators(
    center: Offset,
    outerRadius: Float,
    count: Int,
    startAngle: Float,
    sweepAngle: Float,
    foregroundColor: Color
) {
    val indicatorLength = 8.dp.toPx()
    val indicatorWidth = 2.dp.toPx()
    val step = sweepAngle / count

    for (i in 0..count) {
        val angle = startAngle + (i * step)
        val rad = Math.toRadians(angle.toDouble())

        val startX = center.x + (outerRadius - indicatorLength) * cos(rad).toFloat()
        val startY = center.y + (outerRadius - indicatorLength) * sin(rad).toFloat()
        val endX = center.x + outerRadius * cos(rad).toFloat()
        val endY = center.y + outerRadius * sin(rad).toFloat()

        drawLine(
            color = foregroundColor.copy(alpha = 0.6f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = indicatorWidth
        )
    }
}

@Preview(name = "Gauge Chart - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewDSGaugeChartLight() {
    DSJarvisTheme {
        DSGaugeChart(
            value = 75f,
            maxValue = 100f,
            size = 200.dp,
            strokeWidth = 20.dp,
            foregroundColor = DSJarvisTheme.colors.chart.primary,
            backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            gradient = Brush.sweepGradient(
                colors = listOf(
                    DSJarvisTheme.colors.chart.primary,
                    DSJarvisTheme.colors.success.success100
                )
            ),
            showIndicators = true,
            contentDescription = "75% completion gauge"
        )
    }
}

@Preview(
    name = "Gauge Chart - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDSGaugeChartDark() {
    DSJarvisTheme(darkTheme = true) {
        DSGaugeChart(
            value = 85f,
            maxValue = 100f,
            size = 200.dp,
            strokeWidth = 20.dp,
            foregroundColor = DSJarvisTheme.colors.chart.primary,
            backgroundColor = DSJarvisTheme.colors.neutral.neutral20,
            gradient = Brush.sweepGradient(
                colors = listOf(
                    DSJarvisTheme.colors.warning.warning100,
                    DSJarvisTheme.colors.error.error100
                )
            ),
            showIndicators = true,
            contentDescription = "85% health gauge"
        )
    }
}