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

/**
 * Data for donut chart segments
 */
data class DSDonutChartData(
    val value: Float,
    val label: String,
    val color: Color? = null
)

/**
 * Generic donut chart component with customizable colors and animations
 */
@Composable
fun DSDonutChart(
    data: List<DSDonutChartData>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 40.dp,
    colors: List<Color> = DSJarvisTheme.colors.chart.colors,
    backgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    animationDurationMs: Int = 1200,
    contentDescription: String? = null
) {
    val dataKey = remember(data) { data.hashCode() }
    var animationPlayed by remember(dataKey) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "donut_chart_animation"
    )
    
    LaunchedEffect(dataKey) {
        animationPlayed = true
    }

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val accessibilityDesc = contentDescription ?: 
        "Donut chart with ${data.size} segments, total value $total"

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = accessibilityDesc }
            .testTag("DSDonutChart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (data.isNotEmpty() && total > 0) {
                drawDonutChart(
                    data = data,
                    total = total,
                    animationProgress = animationProgress,
                    strokeWidthPx = strokeWidth.toPx(),
                    colors = colors,
                    backgroundColor = backgroundColor
                )
            }
        }
    }
}

private fun DrawScope.drawDonutChart(
    data: List<DSDonutChartData>,
    total: Float,
    animationProgress: Float,
    strokeWidthPx: Float,
    colors: List<Color>,
    backgroundColor: Color
) {
    val radius = (size.minDimension - strokeWidthPx) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)

    // Draw background circle
    drawArc(
        color = backgroundColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
    )

    // Draw segments
    var currentAngle = -90f // Start from top
    
    data.forEachIndexed { index, segment ->
        val sweepAngle = (segment.value / total) * 360f * animationProgress
        val segmentColor = segment.color ?: colors[index % colors.size]
        
        if (sweepAngle > 0) {
            drawArc(
                color = segmentColor,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
            )
        }
        
        currentAngle += (segment.value / total) * 360f
    }
}

@Preview(name = "Donut Chart - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewDSDonutChartLight() {
    DSJarvisTheme {
        val sampleData = listOf(
            DSDonutChartData(35f, "GET", DSJarvisTheme.colors.success.success100),
            DSDonutChartData(25f, "POST", DSJarvisTheme.colors.chart.primary),
            DSDonutChartData(20f, "PUT", DSJarvisTheme.colors.warning.warning100),
            DSDonutChartData(15f, "DELETE", DSJarvisTheme.colors.error.error100),
            DSDonutChartData(5f, "PATCH", DSJarvisTheme.colors.chart.tertiary)
        )
        
        DSDonutChart(
            data = sampleData,
            size = 200.dp,
            strokeWidth = 40.dp,
            contentDescription = "HTTP methods distribution donut chart"
        )
    }
}

@Preview(
    name = "Donut Chart - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDSDonutChartDark() {
    DSJarvisTheme(darkTheme = true) {
        val sampleData = listOf(
            DSDonutChartData(40f, "Mobile", DSJarvisTheme.colors.chart.primary),
            DSDonutChartData(30f, "Web", DSJarvisTheme.colors.chart.secondary),
            DSDonutChartData(20f, "API", DSJarvisTheme.colors.chart.tertiary),
            DSDonutChartData(10f, "Others", DSJarvisTheme.colors.chart.quaternary)
        )
        
        DSDonutChart(
            data = sampleData,
            size = 200.dp,
            strokeWidth = 40.dp,
            contentDescription = "Platform distribution in dark theme"
        )
    }
}