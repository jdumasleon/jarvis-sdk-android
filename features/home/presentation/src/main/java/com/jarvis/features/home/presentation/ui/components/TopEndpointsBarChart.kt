package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EndpointData

/**
 * Top endpoints bar chart with horizontal bars and endpoint details
 */
@Composable
fun TopEndpointsBarChart(
    endpoints: List<EndpointData>,
    modifier: Modifier = Modifier,
    maxItems: Int = 10
) {
    var animationPlayed by remember(endpoints) { mutableStateOf(false) }
    
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar_chart_animation"
    )
    
    LaunchedEffect(endpoints) {
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Top Endpoints",
                    style = DSJarvisTheme.typography.heading.heading5,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                
                DSText(
                    text = "by request count",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
            
            if (endpoints.isNotEmpty()) {
                val topEndpoints = endpoints.take(maxItems)
                val maxRequests = topEndpoints.maxOfOrNull { it.requestCount } ?: 1
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    itemsIndexed(topEndpoints) { index, endpoint ->
                        EndpointBarItem(
                            endpoint = endpoint,
                            maxRequests = maxRequests,
                            animationProgress = animationProgress,
                            rank = index + 1
                        )
                    }
                }
            } else {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "No endpoint data available",
                        style = DSJarvisTheme.typography.body.body2,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }
    }
}

/**
 * Individual endpoint bar item with animated bar and details
 */
@Composable
private fun EndpointBarItem(
    endpoint: EndpointData,
    maxRequests: Int,
    animationProgress: Float,
    rank: Int
) {
    val barProgress = (endpoint.requestCount.toFloat() / maxRequests) * animationProgress
    
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        // Endpoint info header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                // Rank indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(getMethodColor(endpoint.method).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "$rank",
                        style = DSJarvisTheme.typography.body.body3,
                        fontWeight = FontWeight.Bold,
                        color = getMethodColor(endpoint.method)
                    )
                }
                
                // Method badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(getMethodColor(endpoint.method))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    DSText(
                        text = endpoint.method,
                        style = DSJarvisTheme.typography.body.body3,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                // Endpoint path
                DSText(
                    text = endpoint.endpoint.take(30) + if (endpoint.endpoint.length > 30) "..." else "",
                    style = DSJarvisTheme.typography.body.body2,
                    fontWeight = FontWeight.Medium,
                    color = DSJarvisTheme.colors.neutral.neutral90,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            
            // Request count
            DSText(
                text = "${endpoint.requestCount}",
                style = DSJarvisTheme.typography.body.body2,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )
        }
        
        // Animated bar with metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DSJarvisTheme.colors.neutral.neutral20)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    getMethodColor(endpoint.method),
                                    getMethodColor(endpoint.method).copy(alpha = 0.8f)
                                )
                            )
                        )
                )
            }
            
            // Metrics
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                MetricChip(
                    label = "${String.format("%.0f", endpoint.averageResponseTime)}ms",
                    color = if (endpoint.averageResponseTime < 500) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                
                if (endpoint.errorRate > 0) {
                    MetricChip(
                        label = "${String.format("%.1f", endpoint.errorRate)}% err",
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

/**
 * Small metric chip for displaying endpoint metrics
 */
@Composable
private fun MetricChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.body3,
            color = color
        )
    }
}

/**
 * Get color for HTTP method
 */
private fun getMethodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> Color(0xFF4CAF50)
    "POST" -> Color(0xFF2196F3)
    "PUT" -> Color(0xFFFF9800)
    "DELETE" -> Color(0xFFF44336)
    "PATCH" -> Color(0xFF9C27B0)
    "HEAD" -> Color(0xFF607D8B)
    "OPTIONS" -> Color(0xFF795548)
    else -> Color(0xFF9E9E9E)
}

/**
 * Compact horizontal bar chart for dashboard cards
 */
@Composable
fun CompactEndpointsChart(
    endpoints: List<EndpointData>,
    modifier: Modifier = Modifier,
    height: Int = 120
) {
    val maxRequests = endpoints.maxOfOrNull { it.requestCount } ?: 1
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        drawCompactBarChart(
            endpoints = endpoints.take(5),
            maxRequests = maxRequests,
            canvasSize = size
        )
    }
}

/**
 * Draw compact bar chart for endpoints
 */
private fun DrawScope.drawCompactBarChart(
    endpoints: List<EndpointData>,
    maxRequests: Int,
    canvasSize: Size
) {
    if (endpoints.isEmpty()) return
    
    val barHeight = 16.dp.toPx()
    val barSpacing = 8.dp.toPx()
    val padding = 16.dp.toPx()
    
    endpoints.forEachIndexed { index, endpoint ->
        val y = padding + index * (barHeight + barSpacing)
        val barWidth = (endpoint.requestCount.toFloat() / maxRequests) * (canvasSize.width - 2 * padding)
        
        // Background bar
        drawRoundRect(
            color = Color.Gray.copy(alpha = 0.2f),
            topLeft = Offset(padding, y),
            size = Size(canvasSize.width - 2 * padding, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        
        // Progress bar
        drawRoundRect(
            color = getMethodColor(endpoint.method),
            topLeft = Offset(padding, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

/**
 * Top endpoints summary card with bar chart
 */
@Composable
fun TopEndpointsCard(
    endpoints: List<EndpointData>,
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
            // Header with summary stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Top Endpoints",
                    style = DSJarvisTheme.typography.heading.heading4,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                
                if (endpoints.isNotEmpty()) {
                    val totalRequests = endpoints.sumOf { it.requestCount }
                    DSText(
                        text = "$totalRequests total requests",
                        style = DSJarvisTheme.typography.body.body2,
                        color = DSJarvisTheme.colors.neutral.neutral70
                    )
                }
            }
            
            // Bar chart
            TopEndpointsBarChart(
                endpoints = endpoints,
                maxItems = 8
            )
        }
    }
}

// Import DSCard
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