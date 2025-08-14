package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.TimeSeriesDataPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Network requests over time area chart with smooth animations and gradient fill
 */
@Composable
fun NetworkAreaChart(
    dataPoints: List<TimeSeriesDataPoint>,
    modifier: Modifier = Modifier,
    title: String = "Requests Over Time",
    height: Int = 200
) {
    var animationPlayed by remember(dataPoints) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "area_chart_animation"
    )
    
    LaunchedEffect(dataPoints) {
        animationPlayed = true
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
            // Chart title and summary
            NetworkChartHeader(
                title = title,
                dataPoints = dataPoints
            )
            
            // Area chart
            if (dataPoints.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    drawNetworkAreaChart(
                        dataPoints = dataPoints,
                        animationProgress = animationProgress,
                        canvasSize = size
                    )
                }
                
                // Time axis labels
                NetworkTimeAxisLabels(
                    dataPoints = dataPoints,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "No network data available",
                        style = DSJarvisTheme.typography.body.body2,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }
    }
}

/**
 * Chart header with title and summary statistics
 */
@Composable
private fun NetworkChartHeader(
    title: String,
    dataPoints: List<TimeSeriesDataPoint>
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
                val avg = if (dataPoints.isNotEmpty()) total / dataPoints.size else 0
                
                DSText(
                    text = "Total: $total • Avg: $avg req/min",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
        }
        
        // Current trend indicator
        if (dataPoints.size >= 2) {
            val trend = dataPoints.takeLast(2)
            val isIncreasing = trend[1].value > trend[0].value
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                DSText(
                    text = if (isIncreasing) "↗" else "↘",
                    style = DSJarvisTheme.typography.body.body2,
                    color = if (isIncreasing) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                DSText(
                    text = "${trend.last().value.toInt()}",
                    style = DSJarvisTheme.typography.body.body2,
                    fontWeight = FontWeight.Medium,
                    color = DSJarvisTheme.colors.neutral.neutral90
                )
            }
        }
    }
}

/**
 * Time axis labels showing timestamps
 */
@Composable
private fun NetworkTimeAxisLabels(
    dataPoints: List<TimeSeriesDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Show first, middle, and last timestamps
        val indicesToShow = listOf(
            0,
            dataPoints.size / 2,
            dataPoints.size - 1
        ).distinct()
        
        indicesToShow.forEach { index ->
            if (index < dataPoints.size) {
                DSText(
                    text = timeFormat.format(Date(dataPoints[index].timestamp)),
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

/**
 * Draws the area chart with smooth curves and gradient fill
 */
private fun DrawScope.drawNetworkAreaChart(
    dataPoints: List<TimeSeriesDataPoint>,
    animationProgress: Float,
    canvasSize: Size
) {
    if (dataPoints.isEmpty()) return
    
    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 0f
    val minValue = dataPoints.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue
    
    if (valueRange == 0f) return
    
    val width = canvasSize.width
    val height = canvasSize.height
    val padding = 20.dp.toPx()
    
    // Calculate points for the area chart
    val points = dataPoints.mapIndexed { index, dataPoint ->
        val x = padding + (index.toFloat() / (dataPoints.size - 1)) * (width - 2 * padding)
        val normalizedValue = (dataPoint.value - minValue) / valueRange
        val y = height - padding - (normalizedValue * (height - 2 * padding))
        
        Offset(x, y)
    }
    
    // Apply animation by limiting the number of points drawn
    val animatedPointCount = (points.size * animationProgress).toInt().coerceAtLeast(1)
    val animatedPoints = points.take(animatedPointCount)
    
    if (animatedPoints.size < 2) return
    
    // Create path for area fill
    val fillPath = Path().apply {
        moveTo(animatedPoints.first().x, height - padding)
        animatedPoints.forEach { point ->
            lineTo(point.x, point.y)
        }
        lineTo(animatedPoints.last().x, height - padding)
        close()
    }
    
    // Create gradient for area fill
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2196F3).copy(alpha = 0.3f),
            Color(0xFF2196F3).copy(alpha = 0.05f)
        ),
        startY = 0f,
        endY = height
    )
    
    // Draw area fill
    drawPath(
        path = fillPath,
        brush = gradient
    )
    
    // Create path for stroke line
    val strokePath = Path().apply {
        moveTo(animatedPoints.first().x, animatedPoints.first().y)
        animatedPoints.drop(1).forEach { point ->
            lineTo(point.x, point.y)
        }
    }
    
    // Draw stroke line
    drawPath(
        path = strokePath,
        color = Color(0xFF2196F3),
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    
    // Draw data points
    animatedPoints.forEach { point ->
        drawCircle(
            color = Color(0xFF2196F3),
            radius = 4.dp.toPx(),
            center = point
        )
        
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = point
        )
    }
    
    // Draw value labels for key points
    if (animatedPoints.isNotEmpty()) {
        val firstPoint = animatedPoints.first()
        val lastPoint = animatedPoints.last()
        
        // You can add text drawing here if needed
        // For now, we'll keep it simple without text to avoid complex text measurement
    }
}

/**
 * Network overview card with multiple metrics
 */
@Composable
fun NetworkOverviewCard(
    dataPoints: List<TimeSeriesDataPoint>,
    totalRequests: Int,
    averageResponseTime: Float,
    errorRate: Float,
    modifier: Modifier = Modifier
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
                text = "Network Overview",
                style = DSJarvisTheme.typography.heading.heading4,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
            
            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetworkMetricItem(
                    label = "Requests",
                    value = totalRequests.toString(),
                    color = DSJarvisTheme.colors.primary.primary50
                )
                
                NetworkMetricItem(
                    label = "Avg Time",
                    value = "${String.format("%.0f", averageResponseTime)}ms",
                    color = DSJarvisTheme.colors.secondary.secondary50
                )
                
                NetworkMetricItem(
                    label = "Error Rate",
                    value = "${String.format("%.1f", errorRate)}%",
                    color = if (errorRate < 5f) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            // Area chart
            NetworkAreaChart(
                dataPoints = dataPoints,
                title = "Request Timeline",
                height = 150
            )
        }
    }
}

/**
 * Individual network metric display item
 */
@Composable
private fun NetworkMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DSText(
            text = value,
            style = DSJarvisTheme.typography.heading.heading5,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.body3,
            color = DSJarvisTheme.colors.neutral.neutral70
        )
    }
}

// Import DSCard from design system (reusing from HealthScoreGauge.kt)
@Composable
private fun DSCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = DSJarvisTheme.shapes.m,
    elevation: androidx.compose.ui.unit.Dp = DSJarvisTheme.elevations.level1,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        shape = shape,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = elevation),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = DSJarvisTheme.colors.extra.background
        ),
        content = { content() }
    )
}