package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.jarvis.features.home.domain.entity.HttpMethodData
import kotlin.math.cos
import kotlin.math.sin

/**
 * HTTP methods distribution donut chart with legend and animations
 */
@Composable
fun HttpMethodsDonutChart(
    httpMethods: List<HttpMethodData>,
    modifier: Modifier = Modifier,
    size: Int = 180
) {
    var animationPlayed by remember(httpMethods) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "donut_chart_animation"
    )
    
    LaunchedEffect(httpMethods) {
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
            DSText(
                text = "HTTP Methods",
                style = DSJarvisTheme.typography.heading.heading5,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
            
            if (httpMethods.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut chart
                    Box(
                        modifier = Modifier.size(size.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawHttpMethodsDonut(
                                httpMethods = httpMethods,
                                animationProgress = animationProgress,
                                canvasSize = this.size
                            )
                        }
                        
                        // Center text with total requests
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val totalRequests = httpMethods.sumOf { it.count }
                            DSText(
                                text = "$totalRequests",
                                style = DSJarvisTheme.typography.heading.heading4,
                                fontWeight = FontWeight.Bold,
                                color = DSJarvisTheme.colors.neutral.neutral100
                            )
                            DSText(
                                text = "Requests",
                                style = DSJarvisTheme.typography.body.body3,
                                color = DSJarvisTheme.colors.neutral.neutral70
                            )
                        }
                    }
                    
                    // Legend
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                    ) {
                        httpMethods.take(6).forEach { methodData ->
                            HttpMethodLegendItem(
                                methodData = methodData,
                                animationProgress = animationProgress
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(size.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "No HTTP method data available",
                        style = DSJarvisTheme.typography.body.body2,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
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
    val animatedCount by animateFloatAsState(
        targetValue = if (animationProgress > 0f) methodData.count.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800),
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
                .background(Color(android.graphics.Color.parseColor(methodData.color)))
        )
        
        // Method name and stats
        Column(modifier = Modifier.weight(1f)) {
            DSText(
                text = methodData.method,
                style = DSJarvisTheme.typography.body.body2,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral90
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                DSText(
                    text = "${animatedCount.toInt()}",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
                
                DSText(
                    text = "•",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
                
                DSText(
                    text = "${String.format("%.1f", methodData.percentage)}%",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
        }
        
        // Average response time
        DSText(
            text = "${String.format("%.0f", methodData.averageResponseTime)}ms",
            style = DSJarvisTheme.typography.body.body3,
            color = Color(android.graphics.Color.parseColor(methodData.color))
        )
    }
}

/**
 * Draws the donut chart with segments for each HTTP method
 */
private fun DrawScope.drawHttpMethodsDonut(
    httpMethods: List<HttpMethodData>,
    animationProgress: Float,
    canvasSize: Size
) {
    if (httpMethods.isEmpty()) return
    
    val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
    val radius = canvasSize.minDimension / 2 * 0.8f
    val strokeWidth = radius * 0.3f
    val innerRadius = radius - strokeWidth
    
    var startAngle = -90f // Start from top
    
    httpMethods.forEach { methodData ->
        val sweepAngle = (methodData.percentage / 100f) * 360f * animationProgress
        
        // Draw arc segment
        drawArc(
            color = Color(android.graphics.Color.parseColor(methodData.color)),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        
        startAngle += sweepAngle
    }
    
    // Draw inner circle for donut effect
    drawCircle(
        color = Color.White,
        radius = innerRadius,
        center = center
    )
}

/**
 * Expanded HTTP methods card with additional metrics
 */
@Composable
fun HttpMethodsCard(
    httpMethods: List<HttpMethodData>,
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
            // Header with summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "HTTP Methods",
                    style = DSJarvisTheme.typography.heading.heading4,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                
                DSText(
                    text = "${httpMethods.size} methods",
                    style = DSJarvisTheme.typography.body.body2,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
            
            // Donut chart
            HttpMethodsDonutChart(
                httpMethods = httpMethods,
                size = 200
            )
            
            // Detailed list
            if (httpMethods.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSText(
                        text = "Method Details",
                        style = DSJarvisTheme.typography.body.body2,
                        fontWeight = FontWeight.Medium,
                        color = DSJarvisTheme.colors.neutral.neutral90
                    )
                    
                    httpMethods.forEach { methodData ->
                        HttpMethodDetailItem(methodData = methodData)
                    }
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
                    .background(Color(android.graphics.Color.parseColor(methodData.color)))
            )
            
            DSText(
                text = methodData.method,
                style = DSJarvisTheme.typography.body.body2,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral90
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = "${methodData.count}",
                style = DSJarvisTheme.typography.body.body2,
                color = DSJarvisTheme.colors.neutral.neutral70
            )
            
            DSText(
                text = "${String.format("%.1f", methodData.percentage)}%",
                style = DSJarvisTheme.typography.body.body2,
                color = DSJarvisTheme.colors.neutral.neutral70
            )
            
            DSText(
                text = "${String.format("%.0f", methodData.averageResponseTime)}ms",
                style = DSJarvisTheme.typography.body.body2,
                color = Color(android.graphics.Color.parseColor(methodData.color))
            )
        }
    }
}

// Import DSCard and other components
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