package com.jarvis.features.home.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.domain.performance.CpuMetrics
import com.jarvis.core.domain.performance.FpsMetrics
import com.jarvis.core.domain.performance.FpsStability
import com.jarvis.core.domain.performance.MemoryMetrics
import com.jarvis.core.domain.performance.MemoryPressure
import com.jarvis.core.domain.performance.PerformanceSnapshot
import kotlin.math.roundToInt

/**
 * Performance overview chart component showing key metrics in a compact format
 */
@Composable
fun PerformanceOverviewCardChart(
    performanceSnapshot: PerformanceSnapshot?,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level2
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = "Performance Monitor",
                style = DSJarvisTheme.typography.title.large,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            if (performanceSnapshot != null) {
                PerformanceOverviewCharts(
                    performanceSnapshot = performanceSnapshot
                )
            } else {
                DSText(
                    text = "No performance data available",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral60,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun PerformanceOverviewCharts(
    performanceSnapshot: PerformanceSnapshot,
    modifier: Modifier = Modifier
) {
    var isUpdating by remember { mutableStateOf(false) }
    
    // Visual feedback for real-time updates
    LaunchedEffect(performanceSnapshot.timestamp) {
        isUpdating = true
        delay(300) // Show update indicator for 300ms
        isUpdating = false
    }

    Column(modifier = modifier) {
        // Header with real-time indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUpdating) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF10B981), CircleShape) // Green dot
                )
            } else {
                DSText(
                    text = "Live",
                    style = DSJarvisTheme.typography.body.small,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
    
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
        // CPU Metric
        performanceSnapshot.cpuUsage?.let { cpu ->
            PerformanceMetricItem(
                modifier = Modifier.weight(1f),
                title = "CPU",
                value = "${cpu.cpuUsagePercent.roundToInt()}%",
                icon = Icons.Default.Speed,
                color = getCpuColor(cpu.cpuUsagePercent),
                progress = cpu.cpuUsagePercent / 100f
            )
        }

        // Memory Metric
        performanceSnapshot.memoryUsage?.let { memory ->
            PerformanceMetricItem(
                modifier = Modifier.weight(1f),
                title = "Memory",
                value = "${memory.heapUsagePercent.roundToInt()}%",
                icon = Icons.Default.Memory,
                color = getMemoryColor(memory.memoryPressure),
                progress = memory.heapUsagePercent / 100f
            )
        }

        // FPS Metric
        performanceSnapshot.fpsMetrics?.let { fps ->
            PerformanceMetricItem(
                modifier = Modifier.weight(1f),
                title = "FPS",
                value = "${fps.currentFps.roundToInt()}",
                icon = Icons.Default.MonitorHeart,
                color = getFpsColor(fps.fpsStability),
                progress = fps.currentFps / fps.refreshRate
            )
        }
        }
    }
}

/**
 * Individual performance metric display component
 */
@Composable
private fun PerformanceMetricItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Value
        DSText(
            text = value,
            style = DSJarvisTheme.typography.title.large,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        // Title
        DSText(
            text = title,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        
        // Progress indicator
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(DSJarvisTheme.shapes.xs),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

/**
 * Detailed CPU chart component
 */
@Composable
fun CpuChart(
    cpuMetrics: CpuMetrics?,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level1
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSText(
                text = "CPU Usage",
                style = DSJarvisTheme.typography.body.large,
                fontWeight = FontWeight.Medium
            )
            
            if (cpuMetrics != null) {
                MetricRow("App CPU", "${cpuMetrics.appCpuUsagePercent.roundToInt()}%")
                MetricRow("System CPU", "${cpuMetrics.systemCpuUsagePercent.roundToInt()}%")
                MetricRow("Cores", "${cpuMetrics.cores}")
                MetricRow("Threads", "${cpuMetrics.threadCount}")
                
                Spacer(Modifier.height(DSJarvisTheme.spacing.s))
                
                LinearProgressIndicator(
                    progress = { cpuMetrics.cpuUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(DSJarvisTheme.shapes.xs),
                    color = getCpuColor(cpuMetrics.cpuUsagePercent)
                )
            } else {
                DSText(
                    text = "No CPU data available",
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

/**
 * Detailed Memory chart component
 */
@Composable
fun MemoryChart(
    memoryMetrics: MemoryMetrics?,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level1
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSText(
                text = "Memory Usage",
                style = DSJarvisTheme.typography.body.large,
                fontWeight = FontWeight.Medium
            )
            
            if (memoryMetrics != null) {
                MetricRow("Heap Used", "${memoryMetrics.heapUsedMB.roundToInt()} MB")
                MetricRow("Heap Max", "${memoryMetrics.heapMaxMB.roundToInt()} MB")
                MetricRow("Native Heap", "${memoryMetrics.nativeHeapUsedMB.roundToInt()} MB")
                MetricRow("Available", "${memoryMetrics.availableMemoryMB.roundToInt()} MB")
                MetricRow("Pressure", memoryMetrics.memoryPressure.name)
                
                Spacer(Modifier.height(DSJarvisTheme.spacing.s))
                
                LinearProgressIndicator(
                    progress = { memoryMetrics.heapUsagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(DSJarvisTheme.shapes.xs),
                    color = getMemoryColor(memoryMetrics.memoryPressure)
                )
            } else {
                DSText(
                    text = "No memory data available",
                    color = DSJarvisTheme.colors.neutral.neutral60
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Color helper functions
private fun getCpuColor(cpuUsage: Float): Color {
    return when {
        cpuUsage > 80f -> Color(0xFFEF4444) // Red
        cpuUsage > 60f -> Color(0xFFF59E0B) // Orange
        cpuUsage > 40f -> Color(0xFFEAB308) // Yellow
        else -> Color(0xFF10B981) // Green
    }
}

private fun getMemoryColor(pressure: MemoryPressure): Color {
    return when (pressure) {
        MemoryPressure.CRITICAL -> Color(0xFFEF4444) // Red
        MemoryPressure.HIGH -> Color(0xFFF59E0B) // Orange
        MemoryPressure.MODERATE -> Color(0xFFEAB308) // Yellow
        MemoryPressure.LOW -> Color(0xFF10B981) // Green
    }
}

private fun getFpsColor(stability: FpsStability): Color {
    return when (stability) {
        FpsStability.EXCELLENT -> Color(0xFF10B981) // Green
        FpsStability.GOOD -> Color(0xFFEAB308) // Yellow
        FpsStability.FAIR -> Color(0xFFF59E0B) // Orange
        FpsStability.POOR -> Color(0xFFEF4444) // Red
    }
}

// Preview composables
@Preview(showBackground = true)
@Composable
private fun PerformanceOverviewChartPreview() {
    DSJarvisTheme {
        PerformanceOverviewCardChart(
            performanceSnapshot = PerformanceSnapshot(
                cpuUsage = CpuMetrics(
                    cpuUsagePercent = 65.0f,
                    appCpuUsagePercent = 25.0f,
                    systemCpuUsagePercent = 40.0f,
                    cores = 8,
                    threadCount = 42
                ),
                memoryUsage = MemoryMetrics(
                    heapUsedMB = 150.0f,
                    heapTotalMB = 200.0f,
                    heapMaxMB = 256.0f,
                    nativeHeapUsedMB = 80.0f,
                    nativeHeapTotalMB = 120.0f,
                    availableMemoryMB = 1024.0f,
                    totalMemoryMB = 2048.0f,
                    memoryPressure = MemoryPressure.MODERATE
                ),
                fpsMetrics = FpsMetrics(
                    currentFps = 58.0f,
                    averageFps = 55.0f,
                    minFps = 45.0f,
                    maxFps = 60.0f,
                    frameDrops = 3,
                    jankFrames = 12,
                    refreshRate = 60.0f
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CpuChartPreview() {
    DSJarvisTheme {
        CpuChart(
            cpuMetrics = CpuMetrics(
                cpuUsagePercent = 65.0f,
                appCpuUsagePercent = 25.0f,
                systemCpuUsagePercent = 40.0f,
                cores = 8,
                threadCount = 42
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun MemoryChartPreview() {
    DSJarvisTheme {
        MemoryChart(
            memoryMetrics = MemoryMetrics(
                heapUsedMB = 150.0f,
                heapTotalMB = 200.0f,
                heapMaxMB = 256.0f,
                nativeHeapUsedMB = 80.0f,
                nativeHeapTotalMB = 120.0f,
                availableMemoryMB = 1024.0f,
                totalMemoryMB = 2048.0f,
                memoryPressure = MemoryPressure.MODERATE
            )
        )
    }
}