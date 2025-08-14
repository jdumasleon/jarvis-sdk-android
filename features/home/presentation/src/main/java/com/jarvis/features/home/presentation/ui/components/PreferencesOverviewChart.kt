package com.jarvis.features.home.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EnhancedPreferencesMetrics
import com.jarvis.features.home.domain.entity.PreferenceTypeData
import com.jarvis.features.home.domain.entity.PreferenceSizeData
import kotlin.math.cos
import kotlin.math.sin

/**
 * Preferences overview component with type distribution and storage analytics
 */
@Composable
fun PreferencesOverviewChart(
    preferencesMetrics: EnhancedPreferencesMetrics,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember(preferencesMetrics) { mutableStateOf(false) }
    
    LaunchedEffect(preferencesMetrics) {
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
            // Header with total preferences
            PreferencesHeader(preferencesMetrics = preferencesMetrics)
            
            // Type distribution pie chart
            if (preferencesMetrics.typeDistribution.isNotEmpty()) {
                PreferencesTypeChart(
                    typeDistribution = preferencesMetrics.typeDistribution,
                    animationPlayed = animationPlayed
                )
            }
            
            // Storage usage information
            StorageUsageSection(
                storageUsage = preferencesMetrics.storageUsage,
                animationPlayed = animationPlayed
            )
            
            // Size distribution
            if (preferencesMetrics.sizeDistribution.isNotEmpty()) {
                PreferencesSizeDistribution(
                    sizeDistribution = preferencesMetrics.sizeDistribution,
                    animationPlayed = animationPlayed
                )
            }
        }
    }
}

/**
 * Header section with total preferences and summary stats
 */
@Composable
private fun PreferencesHeader(
    preferencesMetrics: EnhancedPreferencesMetrics
) {
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
                imageVector = Icons.Default.Storage,
                contentDescription = "Preferences",
                tint = DSJarvisTheme.colors.primary.primary50,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                DSText(
                    text = "Preferences Overview",
                    style = DSJarvisTheme.typography.heading.heading5,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                
                DSText(
                    text = "${preferencesMetrics.totalPreferences} preferences across ${preferencesMetrics.typeDistribution.size} storage types",
                    style = DSJarvisTheme.typography.body.body3,
                    color = DSJarvisTheme.colors.neutral.neutral70
                )
            }
        }
        
        // Storage efficiency score
        StorageEfficiencyBadge(
            efficiency = preferencesMetrics.storageUsage.storageEfficiency
        )
    }
}

/**
 * Storage efficiency badge
 */
@Composable
private fun StorageEfficiencyBadge(
    efficiency: Float
) {
    val color = when {
        efficiency >= 80f -> Color(0xFF4CAF50)
        efficiency >= 60f -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        DSText(
            text = "${String.format("%.0f", efficiency)}% efficient",
            style = DSJarvisTheme.typography.body.body3,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Preferences type distribution pie chart
 */
@Composable
private fun PreferencesTypeChart(
    typeDistribution: List<PreferenceTypeData>,
    animationPlayed: Boolean
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "type_chart_animation"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Storage Type Distribution",
            style = DSJarvisTheme.typography.body.body1,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral90
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pie chart
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPreferencesTypePieChart(
                        typeDistribution = typeDistribution,
                        animationProgress = animationProgress,
                        canvasSize = size
                    )
                }
            }
            
            // Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                typeDistribution.take(4).forEach { typeData ->
                    PreferenceTypeLegendItem(
                        typeData = typeData,
                        animationProgress = animationProgress
                    )
                }
            }
        }
    }
}

/**
 * Legend item for preference type
 */
@Composable
private fun PreferenceTypeLegendItem(
    typeData: PreferenceTypeData,
    animationProgress: Float
) {
    val animatedCount by animateFloatAsState(
        targetValue = if (animationProgress > 0f) typeData.count.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "type_count_animation"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(typeData.color)))
        )
        
        // Type info
        Column(modifier = Modifier.weight(1f)) {
            DSText(
                text = typeData.type,
                style = DSJarvisTheme.typography.body.body2,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral90
            )
            
            DSText(
                text = "${animatedCount.toInt()} prefs • ${formatBytes(typeData.totalSize)}",
                style = DSJarvisTheme.typography.body.body3,
                color = DSJarvisTheme.colors.neutral.neutral70
            )
        }
        
        // Percentage
        DSText(
            text = "${String.format("%.1f", typeData.percentage)}%",
            style = DSJarvisTheme.typography.body.body3,
            color = Color(android.graphics.Color.parseColor(typeData.color))
        )
    }
}

/**
 * Storage usage section with detailed metrics
 */
@Composable
private fun StorageUsageSection(
    storageUsage: com.jarvis.features.home.domain.entity.StorageUsageData,
    animationPlayed: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Storage Usage",
            style = DSJarvisTheme.typography.body.body1,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral90
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StorageMetricItem(
                label = "Total Size",
                value = formatBytes(storageUsage.totalSize),
                color = DSJarvisTheme.colors.primary.primary50,
                animationPlayed = animationPlayed
            )
            
            StorageMetricItem(
                label = "Avg Size",
                value = formatBytes(storageUsage.averageSize),
                color = DSJarvisTheme.colors.secondary.secondary50,
                animationPlayed = animationPlayed
            )
            
            StorageMetricItem(
                label = "Efficiency",
                value = "${String.format("%.0f", storageUsage.storageEfficiency)}%",
                color = when {
                    storageUsage.storageEfficiency >= 80f -> Color(0xFF4CAF50)
                    storageUsage.storageEfficiency >= 60f -> Color(0xFFFFC107)
                    else -> Color(0xFFFF9800)
                },
                animationPlayed = animationPlayed
            )
        }
        
        // Largest preference info
        storageUsage.largestPreference?.let { largestPref ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DSJarvisTheme.colors.neutral.neutral10)
                    .padding(DSJarvisTheme.spacing.m)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
                ) {
                    DSText(
                        text = "Largest Preference",
                        style = DSJarvisTheme.typography.body.body3,
                        color = DSJarvisTheme.colors.neutral.neutral70
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DSText(
                            text = largestPref.key.take(25) + if (largestPref.key.length > 25) "..." else "",
                            style = DSJarvisTheme.typography.body.body2,
                            fontWeight = FontWeight.Medium,
                            color = DSJarvisTheme.colors.neutral.neutral90,
                            modifier = Modifier.weight(1f)
                        )
                        
                        DSText(
                            text = formatBytes(largestPref.size),
                            style = DSJarvisTheme.typography.body.body2,
                            color = DSJarvisTheme.colors.primary.primary50
                        )
                    }
                }
            }
        }
    }
}

/**
 * Storage metric item with animation
 */
@Composable
private fun StorageMetricItem(
    label: String,
    value: String,
    color: Color,
    animationPlayed: Boolean
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "storage_metric_animation"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(animationProgress)
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

/**
 * Preferences size distribution component
 */
@Composable
private fun PreferencesSizeDistribution(
    sizeDistribution: List<PreferenceSizeData>,
    animationPlayed: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Size Distribution",
            style = DSJarvisTheme.typography.body.body1,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral90
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s),
            contentPadding = PaddingValues(horizontal = DSJarvisTheme.spacing.xs)
        ) {
            items(sizeDistribution) { sizeData ->
                SizeDistributionChip(
                    sizeData = sizeData,
                    animationPlayed = animationPlayed
                )
            }
        }
    }
}

/**
 * Size distribution chip with count and percentage
 */
@Composable
private fun SizeDistributionChip(
    sizeData: PreferenceSizeData,
    animationPlayed: Boolean
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "size_chip_animation"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DSJarvisTheme.colors.primary.primary50.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DSText(
                text = sizeData.sizeRange,
                style = DSJarvisTheme.typography.body.body3,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.primary.primary50
            )
            
            DSText(
                text = "${(sizeData.count * animationProgress).toInt()}",
                style = DSJarvisTheme.typography.body.body2,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral90
            )
            
            DSText(
                text = "${String.format("%.1f", sizeData.percentage * animationProgress)}%",
                style = DSJarvisTheme.typography.body.body3,
                color = DSJarvisTheme.colors.neutral.neutral70
            )
        }
    }
}

/**
 * Draw preferences type pie chart
 */
private fun DrawScope.drawPreferencesTypePieChart(
    typeDistribution: List<PreferenceTypeData>,
    animationProgress: Float,
    canvasSize: Size
) {
    if (typeDistribution.isEmpty()) return
    
    val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
    val radius = canvasSize.minDimension / 2 * 0.8f
    
    var startAngle = -90f // Start from top
    
    typeDistribution.forEach { typeData ->
        val sweepAngle = (typeData.percentage / 100f) * 360f * animationProgress
        
        drawArc(
            color = Color(android.graphics.Color.parseColor(typeData.color)),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
        
        startAngle += sweepAngle
    }
}

/**
 * Format bytes to human readable format
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${String.format("%.1f", bytes / 1024.0)}KB"
        bytes < 1024 * 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))}MB"
        else -> "${String.format("%.1f", bytes / (1024.0 * 1024.0 * 1024.0))}GB"
    }
}

// Import DSCard and alpha modifier
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

private fun Modifier.alpha(alpha: Float): Modifier = this then androidx.compose.ui.Modifier.alpha(alpha)