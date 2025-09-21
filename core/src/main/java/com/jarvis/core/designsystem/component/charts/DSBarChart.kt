package com.jarvis.core.designsystem.component.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Data for bar chart
 */
data class DSBarChartData(
    val value: Float,
    val label: String,
    val color: Color? = null
)

/**
 * Generic bar chart component with animations and customizable styling
 */
@Composable
fun DSBarChart(
    data: List<DSBarChartData>,
    modifier: Modifier = Modifier,
    orientation: ChartOrientation = ChartOrientation.Vertical,
    colors: List<Color> = DSJarvisTheme.colors.chart.colors,
    backgroundColor: Color = DSJarvisTheme.colors.extra.surface,
    cornerRadius: Dp = 4.dp,
    barSpacing: Dp = 8.dp,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    showGrid: Boolean = true,
    gridColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    animationDurationMs: Int = 1000,
    contentDescription: String? = null
) {
    val dataKey = remember(data) { data.hashCode() }
    var animationPlayed by remember(dataKey) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "bar_chart_animation"
    )
    
    LaunchedEffect(dataKey) {
        animationPlayed = true
    }

    val accessibilityDesc = contentDescription ?: 
        "Bar chart with ${data.size} bars"

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = accessibilityDesc }
            .testTag("DSBarChart")
    ) {
        if (data.isNotEmpty()) {
            drawBarChart(
                data = data,
                animationProgress = animationProgress,
                canvasSize = size,
                orientation = orientation,
                colors = colors,
                backgroundColor = backgroundColor,
                cornerRadiusPx = cornerRadius.toPx(),
                barSpacingPx = barSpacing.toPx(),
                paddingValues = paddingValues,
                showGrid = showGrid,
                gridColor = gridColor
            )
        }
    }
}

@Preview(name = "Bar Chart Vertical - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewDSBarChartVerticalLight() {
    DSJarvisTheme {
        val sampleData = listOf(
            DSBarChartData(25f, "/api/users"),
            DSBarChartData(35f, "/api/posts"),
            DSBarChartData(18f, "/api/comments"),
            DSBarChartData(42f, "/api/auth"),
            DSBarChartData(28f, "/api/search")
        )
        
        DSBarChart(
            data = sampleData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            orientation = ChartOrientation.Vertical,
            showGrid = true,
            contentDescription = "Top endpoints bar chart"
        )
    }
}

@Preview(
    name = "Bar Chart Vertical - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDSBarChartVerticalDark() {
    DSJarvisTheme(darkTheme = true) {
        val sampleData = listOf(
            DSBarChartData(32f, "iOS"),
            DSBarChartData(28f, "Android"),
            DSBarChartData(22f, "Web"),
            DSBarChartData(15f, "Desktop"),
            DSBarChartData(8f, "Others")
        )
        
        DSBarChart(
            data = sampleData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            orientation = ChartOrientation.Vertical,
            showGrid = true,
            contentDescription = "Platform usage in dark theme"
        )
    }
}

@Preview(
    name = "Bar Chart Horizontal - Dark", 
    showBackground = true, 
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDSBarChartHorizontalDark() {
    DSJarvisTheme(darkTheme = true) {
        val sampleData = listOf(
            DSBarChartData(45f, "Network"),
            DSBarChartData(35f, "CPU"),
            DSBarChartData(25f, "Memory"),
            DSBarChartData(20f, "Storage")
        )
        
        DSBarChart(
            data = sampleData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            orientation = ChartOrientation.Horizontal,
            showGrid = true,
            contentDescription = "Resource usage horizontal bar chart"
        )
    }
}

enum class ChartOrientation {
    Vertical, Horizontal
}

private fun DrawScope.drawBarChart(
    data: List<DSBarChartData>,
    animationProgress: Float,
    canvasSize: Size,
    orientation: ChartOrientation,
    colors: List<Color>,
    backgroundColor: Color,
    cornerRadiusPx: Float,
    barSpacingPx: Float,
    paddingValues: PaddingValues,
    showGrid: Boolean,
    gridColor: Color
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

    val maxValue = data.maxOfOrNull { it.value } ?: 1f

    // Draw grid
    if (showGrid) {
        drawGrid(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            gridColor = gridColor,
            orientation = orientation
        )
    }

    when (orientation) {
        ChartOrientation.Vertical -> {
            val barWidth = (width - (data.size - 1) * barSpacingPx) / data.size
            
            data.forEachIndexed { index, item ->
                val barHeight = (item.value / maxValue) * height * animationProgress
                val barX = left + index * (barWidth + barSpacingPx)
                val barY = bottom - barHeight
                val barColor = item.color ?: colors[index % colors.size]
                
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            }
        }
        ChartOrientation.Horizontal -> {
            val barHeight = (height - (data.size - 1) * barSpacingPx) / data.size
            
            data.forEachIndexed { index, item ->
                val barWidth = (item.value / maxValue) * width * animationProgress
                val barX = left
                val barY = top + index * (barHeight + barSpacingPx)
                val barColor = item.color ?: colors[index % colors.size]
                
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx)
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
    gridColor: Color,
    orientation: ChartOrientation
) {
    val gridLines = 4
    
    when (orientation) {
        ChartOrientation.Vertical -> {
            // Draw horizontal grid lines
            val step = (bottom - top) / gridLines
            for (i in 0..gridLines) {
                val y = top + i * step
                drawLine(
                    color = gridColor,
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        ChartOrientation.Horizontal -> {
            // Draw vertical grid lines
            val step = (right - left) / gridLines
            for (i in 0..gridLines) {
                val x = left + i * step
                drawLine(
                    color = gridColor,
                    start = Offset(x, top),
                    end = Offset(x, bottom),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}