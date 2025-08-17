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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.home.domain.entity.EnhancedPreferencesMetrics
import com.jarvis.features.home.domain.entity.EnhancedPreferencesMetricsMock.mockEnhancedPreferencesMetrics
import com.jarvis.features.home.domain.entity.PreferenceSizeData
import com.jarvis.features.home.domain.entity.PreferenceTypeData
import com.jarvis.features.home.domain.entity.StorageUsageData
import kotlin.math.max

/**
 * Preferences overview component: donut type distribution, storage analytics, and size breakdown.
 */
@Composable
fun PreferencesOverviewChart(
    preferencesMetrics: EnhancedPreferencesMetrics,
    modifier: Modifier = Modifier,
    donutSize: Dp = 140.dp,
    donutStroke: Dp = 18.dp
) {
    var animationPlayed by remember(preferencesMetrics) { mutableStateOf(false) }
    LaunchedEffect(preferencesMetrics) { animationPlayed = true }

    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.l,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier
                .padding(DSJarvisTheme.spacing.l)
                .testTag("PreferencesOverviewChart"),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            // Header with total preferences and efficiency badge
            PreferencesHeader(preferencesMetrics = preferencesMetrics)

            // Storage type donut chart + legend
            if (preferencesMetrics.typeDistribution.isNotEmpty()) {
                PreferencesTypeSection(
                    typeDistribution = preferencesMetrics.typeDistribution,
                    total = preferencesMetrics.totalPreferences,
                    animationPlayed = animationPlayed,
                    donutSize = donutSize,
                    donutStroke = donutStroke
                )
            }

            // Storage usage statistics
            StorageUsageSection(
                storageUsage = preferencesMetrics.storageUsage,
                animationPlayed = animationPlayed
            )

            // Size distribution chips
            if (preferencesMetrics.sizeDistribution.isNotEmpty()) {
                PreferencesSizeDistribution(
                    sizeDistribution = preferencesMetrics.sizeDistribution,
                    animationPlayed = animationPlayed
                )
            }
        }
    }
}

/* --------------------------- Header --------------------------- */

/**
 * Header section with total preferences and summary stats.
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
                tint = DSJarvisTheme.colors.primary.primary60,
                modifier = Modifier.size(24.dp)
            )

            Column {
                DSText(
                    text = "Preferences Overview",
                    style = DSJarvisTheme.typography.heading.heading5,
                    fontWeight = FontWeight.Bold,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
                val typesCount = preferencesMetrics.typeDistribution.size
                DSText(
                    text = "${preferencesMetrics.totalPreferences} preferences • $typesCount storage ${if (typesCount == 1) "type" else "types"}",
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }

        StorageEfficiencyBadge(
            efficiency = preferencesMetrics.storageUsage.storageEfficiency
        )
    }
}

/**
 * Storage efficiency badge with semantic color scale.
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
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        DSText(
            text = "${String.format("%.0f", efficiency)}% efficient",
            style = DSJarvisTheme.typography.body.small,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/* --------------------------- Type Distribution --------------------------- */

@Composable
private fun PreferencesTypeSection(
    typeDistribution: List<PreferenceTypeData>,
    total: Int,
    animationPlayed: Boolean,
    donutSize: Dp,
    donutStroke: Dp
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "type_chart_animation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
        DSText(
            text = "Storage Type Distribution",
            style = DSJarvisTheme.typography.body.large,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.l),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart with center label
            Box(
                modifier = Modifier
                    .size(donutSize)
                    .semantics {
                        contentDescription = "Donut chart showing distribution by storage type"
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPreferencesTypeDonut(
                        typeDistribution = typeDistribution,
                        animationProgress = animationProgress,
                        canvasSize = size,
                        stroke = donutStroke.toPx()
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    DSText(
                        text = total.toString(),
                        style = DSJarvisTheme.typography.heading.heading4,
                        fontWeight = FontWeight.Bold,
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )
                    DSText(
                        text = "total",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }

            // Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
            ) {
                typeDistribution.forEach { typeData ->
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
 * Legend item for a preference type (color dot, label, counts).
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
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(typeData.color)))
        )

        Column(modifier = Modifier.weight(1f)) {
            DSText(
                text = typeData.type,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.neutral.neutral80
            )
            DSText(
                text = "${animatedCount.toInt()} prefs • ${formatBytes(typeData.totalSize)}",
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }

        DSText(
            text = "${String.format("%.1f", typeData.percentage)}%",
            style = DSJarvisTheme.typography.body.small,
            fontWeight = FontWeight.Medium,
            color = Color(android.graphics.Color.parseColor(typeData.color))
        )
    }
}

/* --------------------------- Storage Usage --------------------------- */

/**
 * Storage usage section with three key metrics and an optional "largest" card.
 */
@Composable
private fun StorageUsageSection(
    storageUsage: StorageUsageData,
    animationPlayed: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)) {
        DSText(
            text = "Storage Usage",
            style = DSJarvisTheme.typography.body.large,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StorageMetricItem(
                label = "Total Size",
                value = formatBytes(storageUsage.totalSize),
                color = DSJarvisTheme.colors.primary.primary60,
                animationPlayed = animationPlayed
            )
            StorageMetricItem(
                label = "Avg Size",
                value = formatBytes(storageUsage.averageSize),
                color = DSJarvisTheme.colors.secondary.secondary60,
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

        storageUsage.largestPreference?.let { largest ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DSJarvisTheme.colors.neutral.neutral20)
                    .padding(DSJarvisTheme.spacing.m)
                    .semantics { contentDescription = "Largest preference item" }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)) {
                    DSText(
                        text = "Largest Preference",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSText(
                            text = largest.key.let { if (it.length > 28) it.take(28) + "…" else it },
                            style = DSJarvisTheme.typography.body.medium,
                            fontWeight = FontWeight.Medium,
                            color = DSJarvisTheme.colors.neutral.neutral80,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(DSJarvisTheme.spacing.s))
                        DSText(
                            text = formatBytes(largest.size),
                            style = DSJarvisTheme.typography.body.medium,
                            color = DSJarvisTheme.colors.primary.primary60
                        )
                    }
                }
            }
        }
    }
}

/**
 * One storage metric with fade-in animation.
 */
@Composable
private fun StorageMetricItem(
    label: String,
    value: String,
    color: Color,
    animationPlayed: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "storage_metric_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        DSText(
            text = value,
            style = DSJarvisTheme.typography.heading.heading5,
            fontWeight = FontWeight.Bold,
            color = color
        )
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

/* --------------------------- Size Distribution --------------------------- */

@Composable
private fun PreferencesSizeDistribution(
    sizeDistribution: List<PreferenceSizeData>,
    animationPlayed: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)) {
        DSText(
            text = "Size Distribution",
            style = DSJarvisTheme.typography.body.large,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.neutral.neutral80
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
 * Size distribution chip with count and percentage and a subtle entrance animation.
 */
@Composable
private fun SizeDistributionChip(
    sizeData: PreferenceSizeData,
    animationPlayed: Boolean
) {
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "size_chip_animation"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DSJarvisTheme.colors.primary.primary60.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics { contentDescription = "Size ${sizeData.sizeRange} chip" }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DSText(
                text = sizeData.sizeRange,
                style = DSJarvisTheme.typography.body.small,
                fontWeight = FontWeight.Medium,
                color = DSJarvisTheme.colors.primary.primary60
            )
            DSText(
                text = "${(sizeData.count * progress).toInt()}",
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral80
            )
            DSText(
                text = "${String.format("%.1f", sizeData.percentage * progress)}%",
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
        }
    }
}

/* --------------------------- Drawing --------------------------- */

/**
 * Donut chart for preferences type distribution (animated sweep).
 */
private fun DrawScope.drawPreferencesTypeDonut(
    typeDistribution: List<PreferenceTypeData>,
    animationProgress: Float,
    canvasSize: Size,
    stroke: Float
) {
    if (typeDistribution.isEmpty()) return

    val radius = (minOf(canvasSize.width, canvasSize.height) - stroke) / 2f
    val topLeft = Offset(
        (canvasSize.width - radius * 2f) / 2f,
        (canvasSize.height - radius * 2f) / 2f
    )
    val arcSize = Size(radius * 2f, radius * 2f)

    var startAngle = -90f // from top
    typeDistribution.forEach { type ->
        val sweepAngle = (type.percentage / 100f) * 360f * animationProgress
        drawArc(
            color = Color(android.graphics.Color.parseColor(type.color)),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        startAngle += sweepAngle
    }

    // Optional: faint base ring for better perception of remaining portion
    val remaining = 360f * (1f - animationProgress)
    if (remaining > 0f) {
        drawArc(
            color = Color.Black.copy(alpha = 0.06f),
            startAngle = startAngle,
            sweepAngle = remaining,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/* --------------------------- Utilities --------------------------- */

/**
 * Format bytes into a human-friendly string.
 */
private fun formatBytes(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < mb -> "${String.format("%.1f", bytes / kb)}KB"
        bytes < gb -> "${String.format("%.1f", bytes / mb)}MB"
        else -> "${String.format("%.1f", bytes / gb)}GB"
    }
}

/* --------------------------- Local DSCard shim --------------------------- */

@Composable
private fun DSCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = DSJarvisTheme.shapes.m,
    elevation: Dp = DSJarvisTheme.elevations.level1,
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

/* --------------------------- Previews --------------------------- */

@Preview(name = "Preferences Overview - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewPreferencesOverviewLight() {
    DSJarvisTheme {
        PreferencesOverviewChart(preferencesMetrics = mockEnhancedPreferencesMetrics)
    }
}

@Preview(name = "Preferences Overview - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewPreferencesOverviewDark() {
    DSJarvisTheme {
        PreferencesOverviewChart(preferencesMetrics = mockEnhancedPreferencesMetrics)
    }
}
