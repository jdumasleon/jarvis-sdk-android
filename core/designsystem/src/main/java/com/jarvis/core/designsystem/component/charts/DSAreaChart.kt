package com.jarvis.core.designsystem.component.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Data point for area chart
 */
data class DSChartDataPoint(
    val x: Float,
    val y: Float,
    val label: String? = null
)

/**
 * Generic area chart component with animations and customizable styling
 */
@Composable
fun DSAreaChart(
    dataPoints: List<DSChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = DSJarvisTheme.colors.chart.primary,
    fillStartColor: Color = lineColor.copy(alpha = 0.3f),
    fillEndColor: Color = lineColor.copy(alpha = 0.1f),
    strokeWidth: Dp = 3.dp,
    showGrid: Boolean = true,
    gridColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    backgroundColor: Color = DSJarvisTheme.colors.extra.surface,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    animationDurationMs: Int = 1200,
    contentDescription: String? = null
) {
    val dataKey = remember(dataPoints) { dataPoints.hashCode() }
    var animationPlayed by remember(dataKey) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "area_chart_animation"
    )
    
    LaunchedEffect(dataKey) {
        animationPlayed = true
    }

    val accessibilityDesc = contentDescription ?: 
        "Area chart with ${dataPoints.size} data points"

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = accessibilityDesc }
            .testTag("DSAreaChart")
    ) {
        if (dataPoints.isNotEmpty()) {
            drawAreaChart(
                dataPoints = dataPoints,
                animationProgress = animationProgress,
                canvasSize = size,
                lineColor = lineColor,
                fillStartColor = fillStartColor,
                fillEndColor = fillEndColor,
                strokeWidthPx = strokeWidth.toPx(),
                backgroundColor = backgroundColor,
                showGrid = showGrid,
                gridColor = gridColor,
                paddingValues = paddingValues
            )
        }
    }
}

private fun DrawScope.drawAreaChart(
    dataPoints: List<DSChartDataPoint>,
    animationProgress: Float,
    canvasSize: Size,
    lineColor: Color,
    fillStartColor: Color,
    fillEndColor: Color,
    strokeWidthPx: Float,
    backgroundColor: Color,
    showGrid: Boolean,
    gridColor: Color,
    paddingValues: PaddingValues
) {
    val paddingLeft = paddingValues.calculateLeftPadding(layoutDirection).toPx()
    val paddingTop = paddingValues.calculateTopPadding().toPx()
    val paddingRight = paddingValues.calculateRightPadding(layoutDirection).toPx()
    val paddingBottom = paddingValues.calculateBottomPadding().toPx()
    
    val left = paddingLeft
    val top = paddingTop
    val right = canvasSize.width - paddingRight
    val bottom = canvasSize.height - paddingBottom
    val width = right - left
    val height = bottom - top

    // Calculate data bounds
    val minX = dataPoints.minOf { it.x }
    val maxX = dataPoints.maxOf { it.x }
    val minY = dataPoints.minOf { it.y }
    val maxY = dataPoints.maxOf { it.y }
    
    val xRange = maxX - minX
    val yRange = maxY - minY

    // Map data points to screen coordinates
    val points = dataPoints.map { point ->
        val x = left + (point.x - minX) / xRange * width
        val y = bottom - (point.y - minY) / yRange * height
        Offset(x, y)
    }

    // Apply animation
    val visibleCount = (points.size * animationProgress).toInt().coerceIn(1, points.size)
    val visiblePoints = points.take(visibleCount)

    clipRect(left, top, right, bottom) {
        // Draw grid
        if (showGrid) {
            drawGrid(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                gridColor = gridColor
            )
        }

        if (visiblePoints.size >= 2) {
            // Create area path
            val areaPath = Path().apply {
                moveTo(visiblePoints.first().x, bottom)
                lineTo(visiblePoints.first().x, visiblePoints.first().y)
                visiblePoints.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(visiblePoints.last().x, bottom)
                close()
            }

            // Create line path
            val linePath = Path().apply {
                moveTo(visiblePoints.first().x, visiblePoints.first().y)
                visiblePoints.forEach { point ->
                    lineTo(point.x, point.y)
                }
            }

            // Draw area fill
            val fillBrush = Brush.verticalGradient(
                colors = listOf(fillStartColor, fillEndColor),
                startY = top,
                endY = bottom
            )
            drawPath(path = areaPath, brush = fillBrush)

            // Draw line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw data points
            visiblePoints.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = backgroundColor,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    gridColor: Color
) {
    val gridLines = 5
    val horizontalStep = (bottom - top) / gridLines
    val verticalStep = (right - left) / gridLines

    // Draw horizontal grid lines
    for (i in 0..gridLines) {
        val y = top + i * horizontalStep
        drawLine(
            color = gridColor,
            start = Offset(left, y),
            end = Offset(right, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Draw vertical grid lines
    for (i in 0..gridLines) {
        val x = left + i * verticalStep
        drawLine(
            color = gridColor,
            start = Offset(x, top),
            end = Offset(x, bottom),
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Preview(name = "Area Chart - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewDSAreaChartLight() {
    DSJarvisTheme {
        val sampleData = listOf(
            DSChartDataPoint(0f, 10f, "00:00"),
            DSChartDataPoint(1f, 25f, "01:00"),
            DSChartDataPoint(2f, 18f, "02:00"),
            DSChartDataPoint(3f, 35f, "03:00"),
            DSChartDataPoint(4f, 42f, "04:00"),
            DSChartDataPoint(5f, 28f, "05:00"),
            DSChartDataPoint(6f, 52f, "06:00"),
            DSChartDataPoint(7f, 48f, "07:00")
        )
        
        DSAreaChart(
            dataPoints = sampleData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            lineColor = DSJarvisTheme.colors.chart.primary,
            showGrid = true,
            contentDescription = "Sample area chart with 8 data points"
        )
    }
}

@Preview(
    name = "Area Chart - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDSAreaChartDark() {
    DSJarvisTheme(darkTheme = true) {
        val sampleData = listOf(
            DSChartDataPoint(0f, 15f, "00:00"),
            DSChartDataPoint(1f, 32f, "01:00"),
            DSChartDataPoint(2f, 28f, "02:00"),
            DSChartDataPoint(3f, 45f, "03:00"),
            DSChartDataPoint(4f, 38f, "04:00"),
            DSChartDataPoint(5f, 55f, "05:00"),
            DSChartDataPoint(6f, 42f, "06:00"),
            DSChartDataPoint(7f, 48f, "07:00")
        )
        
        DSAreaChart(
            dataPoints = sampleData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            lineColor = DSJarvisTheme.colors.chart.secondary,
            showGrid = true,
            contentDescription = "Dark theme area chart with network data"
        )
    }
}