package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.HealthRating
import com.jarvis.features.home.domain.entity.HealthScore
import kotlin.math.cos
import kotlin.math.sin

/**
 * Health score gauge component with smooth animation and gradient colors
 */
@Composable
fun HealthScoreGauge(
    healthScore: HealthScore,
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    val animatedScore by animateFloatAsState(
        targetValue = if (animationPlayed) healthScore.overallScore else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "health_score_animation"
    )
    
    LaunchedEffect(healthScore) {
        animationPlayed = true
    }
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHealthGauge(
                score = animatedScore,
                rating = healthScore.rating,
                size = this.size
            )
        }
        
        // Score text overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DSText(
                text = "${animatedScore.toInt()}",
                style = DSJarvisTheme.typography.heading.heading3,
                fontWeight = FontWeight.Bold,
                color = Color(android.graphics.Color.parseColor(healthScore.rating.color))
            )
            
            DSText(
                text = healthScore.rating.displayName,
                style = DSJarvisTheme.typography.body.body2,
                color = DSJarvisTheme.colors.neutral.neutral70
            )
        }
    }
}

/**
 * Draws the health gauge with gradient arc and indicators
 */
private fun DrawScope.drawHealthGauge(
    score: Float,
    rating: HealthRating,
    size: Size
) {
    val strokeWidth = 20.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Background arc
    drawArc(
        color = Color.Gray.copy(alpha = 0.2f),
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(
            center.x - radius,
            center.y - radius
        ),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Gradient for health score arc
    val gradient = createHealthGradient(score)
    
    // Health score arc
    val sweepAngle = (score / 100f) * 270f
    drawArc(
        brush = gradient,
        startAngle = 135f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(
            center.x - radius,
            center.y - radius
        ),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Draw score indicators
    drawScoreIndicators(center, radius + strokeWidth / 2 + 10.dp.toPx())
}

/**
 * Creates a gradient brush based on health score
 */
private fun createHealthGradient(score: Float): Brush {
    return when {
        score >= 90f -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFF4CAF50), // Green
                Color(0xFF8BC34A)  // Light Green
            )
        )
        score >= 70f -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFF8BC34A), // Light Green
                Color(0xFFCDDC39)  // Lime
            )
        )
        score >= 50f -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFFCDDC39), // Lime
                Color(0xFFFFC107)  // Yellow
            )
        )
        score >= 30f -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFFC107), // Yellow
                Color(0xFFFF9800)  // Orange
            )
        )
        else -> Brush.sweepGradient(
            colors = listOf(
                Color(0xFFFF9800), // Orange
                Color(0xFFF44336)  // Red
            )
        )
    }
}

/**
 * Draws score indicator marks around the gauge
 */
private fun DrawScope.drawScoreIndicators(center: Offset, radius: Float) {
    val indicatorLength = 8.dp.toPx()
    val indicatorWidth = 2.dp.toPx()
    
    for (i in 0..10) {
        val angle = 135f + (i * 27f) // 270 degrees divided by 10 intervals
        val startAngle = Math.toRadians(angle.toDouble())
        
        val startX = center.x + (radius - indicatorLength) * cos(startAngle).toFloat()
        val startY = center.y + (radius - indicatorLength) * sin(startAngle).toFloat()
        val endX = center.x + radius * cos(startAngle).toFloat()
        val endY = center.y + radius * sin(startAngle).toFloat()
        
        val color = when (i) {
            in 0..3 -> Color(0xFFF44336) // Red for 0-30
            in 4..6 -> Color(0xFFFF9800) // Orange for 30-60
            in 7..8 -> Color(0xFFFFC107) // Yellow for 60-80
            else -> Color(0xFF4CAF50)    // Green for 80-100
        }
        
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = indicatorWidth
        )
    }
}

/**
 * Health summary card with gauge and key metrics
 */
@Composable
fun HealthSummaryCard(
    healthScore: HealthScore,
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
                text = "Health Summary",
                style = DSJarvisTheme.typography.heading.heading4,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Health gauge
                HealthScoreGauge(
                    healthScore = healthScore,
                    modifier = Modifier.weight(1f)
                )
                
                // Key metrics
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    HealthMetricItem(
                        label = "Requests",
                        value = "${healthScore.keyMetrics.totalRequests}",
                        color = DSJarvisTheme.colors.primary.primary50
                    )
                    
                    HealthMetricItem(
                        label = "Error Rate",
                        value = "${String.format("%.1f", healthScore.keyMetrics.errorRate)}%",
                        color = if (healthScore.keyMetrics.errorRate < 5f) 
                            Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    
                    HealthMetricItem(
                        label = "Avg Response",
                        value = "${String.format("%.0f", healthScore.keyMetrics.averageResponseTime)}ms",
                        color = DSJarvisTheme.colors.neutral.neutral70
                    )
                    
                    HealthMetricItem(
                        label = "Performance",
                        value = "${String.format("%.0f", healthScore.keyMetrics.performanceScore)}",
                        color = DSJarvisTheme.colors.secondary.secondary50
                    )
                }
            }
        }
    }
}

/**
 * Individual health metric display item
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
            style = DSJarvisTheme.typography.body.body2,
            color = DSJarvisTheme.colors.neutral.neutral70
        )
        
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.body2,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// Import DSCard from design system
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