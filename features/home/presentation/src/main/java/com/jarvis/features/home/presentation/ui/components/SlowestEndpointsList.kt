package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.SlowEndpointData
import java.text.SimpleDateFormat
import java.util.*

/**
 * Slowest endpoints list component for identifying performance bottlenecks
 */
@Composable
fun SlowestEndpointsList(
    slowEndpoints: List<SlowEndpointData>,
    modifier: Modifier = Modifier,
    maxItems: Int = 8
) {
    var animationPlayed by remember(slowEndpoints) { mutableStateOf(false) }
    
    LaunchedEffect(slowEndpoints) {
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
            // Header with warning indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Performance Warning",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    DSText(
                        text = "Slowest Endpoints",
                        style = DSJarvisTheme.typography.heading.heading5,
                        fontWeight = FontWeight.Bold,
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )
                }
                
                if (slowEndpoints.isNotEmpty()) {
                    DSText(
                        text = "Performance bottlenecks",
                        style = DSJarvisTheme.typography.body.body3,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            if (slowEndpoints.isNotEmpty()) {
                val topSlowEndpoints = slowEndpoints.take(maxItems)
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    itemsIndexed(topSlowEndpoints) { index, endpoint ->
                        SlowEndpointItem(
                            endpoint = endpoint,
                            animationPlayed = animationPlayed,
                            rank = index + 1
                        )
                    }
                }
            } else {
                // Empty state - good performance
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                    ) {
                        DSText(
                            text = "🎉",
                            style = DSJarvisTheme.typography.heading.heading3
                        )
                        
                        DSText(
                            text = "No slow endpoints detected",
                            style = DSJarvisTheme.typography.body.body2,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                        
                        DSText(
                            text = "All endpoints are performing well",
                            style = DSJarvisTheme.typography.body.body3,
                            color = DSJarvisTheme.colors.neutral.neutral70
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual slow endpoint item with severity indicator and details
 */
@Composable
private fun SlowEndpointItem(
    endpoint: SlowEndpointData,
    animationPlayed: Boolean,
    rank: Int
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = rank * 100),
        label = "slow_endpoint_animation"
    )
    
    val severityColor = getSeverityColor(endpoint.averageResponseTime)
    val animatedSeverityColor by animateColorAsState(
        targetValue = if (animationPlayed) severityColor else Color.Gray,
        animationSpec = tween(durationMillis = 800),
        label = "severity_color_animation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(animatedSeverityColor.copy(alpha = 0.05f))
            .padding(DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        // Header with rank and endpoint info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                // Severity indicator
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedSeverityColor),
                    contentAlignment = Alignment.Center
                ) {
                    DSText(
                        text = "$rank",
                        style = DSJarvisTheme.typography.body.body2,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
            }
            
            // Response time with animation
            val animatedResponseTime by animateFloatAsState(
                targetValue = if (animationPlayed) endpoint.averageResponseTime else 0f,
                animationSpec = tween(durationMillis = 1000),
                label = "response_time_animation"
            )
            
            DSText(
                text = "${String.format("%.0f", animatedResponseTime)}ms",
                style = DSJarvisTheme.typography.heading.heading5,
                fontWeight = FontWeight.Bold,
                color = animatedSeverityColor
            )
        }
        
        // Endpoint path
        DSText(
            text = endpoint.endpoint,
            style = DSJarvisTheme.typography.body.body2,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral90
        )
        
        // Performance metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
            ) {
                PerformanceMetricChip(
                    label = "P95",
                    value = "${String.format("%.0f", endpoint.p95ResponseTime)}ms",
                    color = animatedSeverityColor
                )
                
                PerformanceMetricChip(
                    label = "Requests",
                    value = "${endpoint.requestCount}",
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
            
            // Last slow request time
            if (endpoint.lastSlowRequest > 0) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                DSText(
                    text = "Last: ${timeFormat.format(Date(endpoint.lastSlowRequest))}",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
        
        // Performance suggestion
        if (endpoint.averageResponseTime > 2000) {
            PerformanceSuggestion(
                responseTime = endpoint.averageResponseTime,
                requestCount = endpoint.requestCount
            )
        }
    }
}

/**
 * Performance metric chip for displaying endpoint metrics
 */
@Composable
private fun PerformanceMetricChip(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.body2,
            fontWeight = FontWeight.Medium,
            color = color
        )
        
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.body3,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/**
 * Performance suggestion based on endpoint metrics
 */
@Composable
private fun PerformanceSuggestion(
    responseTime: Float,
    requestCount: Int
) {
    val suggestion = when {
        responseTime > 5000 -> "Critical: Consider caching or database optimization"
        responseTime > 2000 && requestCount > 100 -> "High traffic endpoint: Consider load balancing"
        responseTime > 1000 -> "Slow response: Review query performance"
        else -> "Monitor for performance degradation"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFFF3E0))
            .padding(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = "💡 $suggestion",
            style = DSJarvisTheme.typography.body.body3,
            color = Color(0xFFE65100)
        )
    }
}

/**
 * Get severity color based on response time
 */
private fun getSeverityColor(responseTime: Float): Color = when {
    responseTime > 5000 -> Color(0xFFF44336)  // Critical - Red
    responseTime > 2000 -> Color(0xFFFF9800)  // High - Orange
    responseTime > 1000 -> Color(0xFFFFC107)  // Medium - Yellow
    responseTime > 500 -> Color(0xFF2196F3)   // Low - Blue
    else -> Color(0xFF4CAF50)                  // Good - Green
}

/**
 * Get color for HTTP method (reused from other components)
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
 * Compact slowest endpoints summary for dashboard cards
 */
@Composable
fun SlowestEndpointsSummary(
    slowEndpoints: List<SlowEndpointData>,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Performance Issues",
                    style = DSJarvisTheme.typography.heading.heading4,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                
                if (slowEndpoints.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        DSText(
                            text = "${slowEndpoints.size} slow",
                            style = DSJarvisTheme.typography.body.body3,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
            
            // Slowest endpoints list
            SlowestEndpointsList(
                slowEndpoints = slowEndpoints,
                maxItems = 5
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