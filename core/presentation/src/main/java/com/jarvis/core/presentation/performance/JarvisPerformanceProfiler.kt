package com.jarvis.core.presentation.performance

import android.util.Log
import android.view.Choreographer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive performance profiler for Jarvis SDK
 * Identifies components causing jank and performance issues
 */
@Singleton
class JarvisPerformanceProfiler @Inject constructor() {
    
    private val profileScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Performance tracking data
    private val componentMetrics = ConcurrentHashMap<String, ComponentMetrics>()
    private val recompositionCounts = ConcurrentHashMap<String, AtomicLong>()
    private val frameDrops = AtomicLong(0)
    private val totalFrames = AtomicLong(0)
    
    // Frame monitoring
    private var choreographerCallback: Choreographer.FrameCallback? = null
    private var lastFrameTime = 0L
    private val targetFrameTime = 16_666_667L // 60fps in nanoseconds
    
    private val _performanceData = MutableStateFlow<PerformanceSnapshot?>(null)
    val performanceData = _performanceData.asStateFlow()
    
    // Critical thresholds
    companion object {
        const val TAG = "JarvisPerformanceProfiler"
        const val CRITICAL_FRAME_TIME_MS = 100L // 100ms+ is critical
        const val WARNING_FRAME_TIME_MS = 32L   // 32ms+ is warning
        const val MAX_RECOMPOSITIONS_PER_SECOND = 60
        const val CRITICAL_MEMORY_THRESHOLD_MB = 100L
    }
    
    data class ComponentMetrics(
        val name: String,
        val recompositionsPerSecond: Double,
        val averageRecomposeTime: Double,
        val maxRecomposeTime: Double,
        val memoryUsageMB: Double,
        val isAnimated: Boolean,
        val severity: PerformanceSeverity,
        val suggestions: List<String>
    )
    
    enum class PerformanceSeverity {
        GOOD, WARNING, CRITICAL, BLOCKING
    }
    
    data class PerformanceSnapshot(
        val timestamp: Long,
        val overallFrameRate: Double,
        val frameDropPercentage: Double,
        val criticalComponents: List<ComponentMetrics>,
        val totalRecompositions: Long,
        val memoryUsageMB: Double,
        val cpuUsagePercent: Double,
        val recommendations: List<String>
    )
    
    /**
     * Start comprehensive performance monitoring
     */
    fun startProfiling() {
        Log.d(TAG, "Starting Jarvis SDK performance profiling...")
        
        startFrameMonitoring()
        startMemoryMonitoring()
        startPerformanceAnalysis()
    }
    
    /**
     * Stop performance monitoring
     */
    fun stopProfiling() {
        Log.d(TAG, "Stopping Jarvis SDK performance profiling...")
        
        choreographerCallback?.let {
            Choreographer.getInstance().removeFrameCallback(it)
            choreographerCallback = null
        }
        
        profileScope.coroutineContext.cancelChildren()
        componentMetrics.clear()
        recompositionCounts.clear()
    }
    
    /**
     * Track component recomposition
     */
    fun trackRecomposition(componentName: String, timeMs: Long = 0L) {
        recompositionCounts.getOrPut(componentName) { AtomicLong(0) }.incrementAndGet()
        
        if (timeMs > WARNING_FRAME_TIME_MS) {
            Log.w(TAG, "Slow recomposition detected: $componentName took ${timeMs}ms")
        }
    }
    
    /**
     * Track component performance metrics
     */
    fun trackComponent(
        name: String,
        isAnimated: Boolean = false,
        recomposeTimeMs: Double = 0.0,
        memoryMB: Double = 0.0
    ) {
        val existing = componentMetrics[name]
        val recompositions = recompositionCounts[name]?.get() ?: 0L
        
        val severity = calculateSeverity(recompositions, recomposeTimeMs, memoryMB, isAnimated)
        val suggestions = generateSuggestions(name, severity, isAnimated, recompositions)
        
        componentMetrics[name] = ComponentMetrics(
            name = name,
            recompositionsPerSecond = recompositions / 60.0, // Rough estimate
            averageRecomposeTime = existing?.averageRecomposeTime?.let { 
                (it + recomposeTimeMs) / 2 
            } ?: recomposeTimeMs,
            maxRecomposeTime = maxOf(existing?.maxRecomposeTime ?: 0.0, recomposeTimeMs),
            memoryUsageMB = memoryMB,
            isAnimated = isAnimated,
            severity = severity,
            suggestions = suggestions
        )
    }
    
    private fun calculateSeverity(
        recompositions: Long,
        recomposeTimeMs: Double,
        memoryMB: Double,
        isAnimated: Boolean
    ): PerformanceSeverity {
        return when {
            recomposeTimeMs > CRITICAL_FRAME_TIME_MS -> PerformanceSeverity.BLOCKING
            recompositions > MAX_RECOMPOSITIONS_PER_SECOND * 2 -> PerformanceSeverity.CRITICAL
            memoryMB > CRITICAL_MEMORY_THRESHOLD_MB -> PerformanceSeverity.CRITICAL
            recomposeTimeMs > WARNING_FRAME_TIME_MS -> PerformanceSeverity.WARNING
            isAnimated && recompositions > MAX_RECOMPOSITIONS_PER_SECOND -> PerformanceSeverity.WARNING
            else -> PerformanceSeverity.GOOD
        }
    }
    
    private fun generateSuggestions(
        name: String,
        severity: PerformanceSeverity,
        isAnimated: Boolean,
        recompositions: Long
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        when {
            name.contains("DSJarvisAssistant") && severity != PerformanceSeverity.GOOD -> {
                suggestions.add("Use DSJarvisAssistantMaterial3 with optimized animation")
                suggestions.add("Reduce animation FPS to 30 or lower")
                suggestions.add("Consider disabling rings animation on low-end devices")
            }
            
            name.contains("Canvas") && recompositions > 60 -> {
                suggestions.add("Move animation logic to draw scope only")
                suggestions.add("Use frame limiting in LaunchedEffect")
                suggestions.add("Cache expensive bitmap operations")
            }
            
            name.contains("List") || name.contains("LazyColumn") -> {
                suggestions.add("Implement lazy loading with proper key()")
                suggestions.add("Use contentType for different item types")
                suggestions.add("Consider using LazyVerticalGrid for performance")
            }
            
            isAnimated && severity == PerformanceSeverity.CRITICAL -> {
                suggestions.add("Replace infinite animations with static alternatives")
                suggestions.add("Use remember {} to cache expensive calculations")
                suggestions.add("Implement adaptive quality based on device performance")
            }
            
            severity == PerformanceSeverity.BLOCKING -> {
                suggestions.add("CRITICAL: This component is blocking the UI thread")
                suggestions.add("Move heavy operations to background threads")
                suggestions.add("Consider completely disabling this component")
            }
        }
        
        return suggestions
    }
    
    private fun startFrameMonitoring() {
        choreographerCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                totalFrames.incrementAndGet()
                
                if (lastFrameTime > 0) {
                    val frameTime = frameTimeNanos - lastFrameTime
                    if (frameTime > targetFrameTime * 2) { // Frame took >33ms
                        frameDrops.incrementAndGet()
                        Log.w(TAG, "Frame drop detected: ${frameTime / 1_000_000}ms")
                    }
                }
                
                lastFrameTime = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(choreographerCallback!!)
    }
    
    private fun startMemoryMonitoring() {
        profileScope.launch(Dispatchers.IO) { // ✅ Use IO thread for memory checks
            var lastMemoryCheck = 0L
            val memoryCheckInterval = 3000L // Check every 3 seconds
            
            while (isActive) {
                try {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastMemoryCheck >= memoryCheckInterval) {
                        val runtime = Runtime.getRuntime()
                        val usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
                        
                        if (usedMemoryMB > CRITICAL_MEMORY_THRESHOLD_MB) {
                            Log.w(TAG, "High memory usage detected: ${usedMemoryMB.toInt()}MB")
                        }
                        lastMemoryCheck = currentTime
                    }
                    
                    delay(1000) // Check every second, but only process every 3 seconds
                } catch (e: Exception) {
                    Log.w(TAG, "Memory monitoring error: ${e.message}")
                    delay(5000) // Longer delay on error
                }
            }
        }
    }
    
    private fun startPerformanceAnalysis() {
        profileScope.launch(Dispatchers.Default) { // ✅ Use background thread for analysis
            var lastSnapshot: PerformanceSnapshot? = null
            var consecutiveHighMemory = 0
            
            while (isActive) {
                try {
                    delay(5000) // ✅ Analyze every 5 seconds for better real-time feel
                    
                    val frameDropPercent = if (totalFrames.get() > 0) {
                        (frameDrops.get().toDouble() / totalFrames.get()) * 100.0
                    } else 0.0
                    
                    val runtime = Runtime.getRuntime()
                    val memoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
                    
                    val criticalComponents = componentMetrics.values
                        .filter { it.severity == PerformanceSeverity.CRITICAL || it.severity == PerformanceSeverity.BLOCKING }
                        .sortedByDescending { it.recompositionsPerSecond }
                    
                    val recommendations = generateOverallRecommendations(frameDropPercent, memoryMB, criticalComponents)
                    
                    val snapshot = PerformanceSnapshot(
                        timestamp = System.currentTimeMillis(),
                        overallFrameRate = 60.0 * (1.0 - frameDropPercent / 100.0),
                        frameDropPercentage = frameDropPercent,
                        criticalComponents = criticalComponents,
                        totalRecompositions = recompositionCounts.values.sumOf { it.get() },
                        memoryUsageMB = memoryMB,
                        cpuUsagePercent = 0.0, // Would need more complex calculation
                        recommendations = recommendations
                    )
                    
                    // ✅ Only emit if significantly different to prevent unnecessary recompositions
                    val shouldEmit = lastSnapshot?.let { last ->
                        kotlin.math.abs(snapshot.frameDropPercentage - last.frameDropPercentage) > 1.0 ||
                        kotlin.math.abs(snapshot.memoryUsageMB - last.memoryUsageMB) > 5.0 ||
                        snapshot.criticalComponents.size != last.criticalComponents.size ||
                        (System.currentTimeMillis() - last.timestamp) > 15000 // Force update every 15s
                    } ?: true
                    
                    if (shouldEmit) {
                        _performanceData.value = snapshot
                        lastSnapshot = snapshot
                    }
                    
                    // ✅ Adaptive monitoring based on performance state
                    if (frameDropPercent > 15.0 || memoryMB > CRITICAL_MEMORY_THRESHOLD_MB) {
                        consecutiveHighMemory++
                        if (consecutiveHighMemory >= 3) {
                            Log.e(TAG, "PERSISTENT PERFORMANCE ISSUE: ${frameDropPercent.toInt()}% drops, ${memoryMB.toInt()}MB memory")
                            consecutiveHighMemory = 0
                        }
                    } else {
                        consecutiveHighMemory = 0
                    }
                    
                    // Log critical issues less frequently
                    if (frameDropPercent > 20.0) {
                        Log.e(TAG, "CRITICAL: ${frameDropPercent.toInt()}% frame drops detected!")
                    }
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Performance analysis error: ${e.message}")
                    delay(10000) // Longer delay on error
                }
            }
        }
    }
    
    private fun generateOverallRecommendations(
        frameDropPercent: Double,
        memoryMB: Double,
        criticalComponents: List<ComponentMetrics>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        when {
            frameDropPercent > 20 -> {
                recommendations.add("URGENT: Severe performance issues detected")
                recommendations.add("Disable all non-essential animations immediately")
                recommendations.add("Consider using static alternatives for animated components")
            }
            
            frameDropPercent > 10 -> {
                recommendations.add("Significant performance degradation detected")
                recommendations.add("Reduce animation complexity and frame rates")
            }
            
            criticalComponents.isNotEmpty() -> {
                recommendations.add("${criticalComponents.size} components causing performance issues")
                recommendations.add("Focus optimization on: ${criticalComponents.take(3).map { it.name }}")
            }
            
            memoryMB > CRITICAL_MEMORY_THRESHOLD_MB -> {
                recommendations.add("High memory usage detected")
                recommendations.add("Clear cached resources and optimize bitmap usage")
            }
        }
        
        return recommendations
    }
    
    /**
     * Get detailed report for debugging
     */
    fun getDetailedReport(): String {
        val report = StringBuilder()
        val snapshot = _performanceData.value
        
        report.appendLine("=== JARVIS SDK PERFORMANCE REPORT ===")
        report.appendLine("Timestamp: ${System.currentTimeMillis()}")
        
        if (snapshot != null) {
            report.appendLine("Overall Frame Rate: ${snapshot.overallFrameRate.toInt()} fps")
            report.appendLine("Frame Drop Percentage: ${snapshot.frameDropPercentage.toInt()}%")
            report.appendLine("Memory Usage: ${snapshot.memoryUsageMB.toInt()}MB")
            report.appendLine("Total Recompositions: ${snapshot.totalRecompositions}")
            
            report.appendLine("\n=== CRITICAL COMPONENTS ===")
            snapshot.criticalComponents.forEach { component ->
                report.appendLine("${component.name}:")
                report.appendLine("  - Severity: ${component.severity}")
                report.appendLine("  - Recompositions/sec: ${component.recompositionsPerSecond.toInt()}")
                report.appendLine("  - Max recompose time: ${component.maxRecomposeTime.toInt()}ms")
                component.suggestions.forEach { suggestion ->
                    report.appendLine("  - SUGGESTION: $suggestion")
                }
                report.appendLine()
            }
            
            report.appendLine("=== RECOMMENDATIONS ===")
            snapshot.recommendations.forEach { rec ->
                report.appendLine("• $rec")
            }
        }
        
        return report.toString()
    }
}

/**
 * Compose extension for tracking component performance
 */
@Composable
fun TrackPerformance(
    componentName: String,
    isAnimated: Boolean = false,
    profiler: JarvisPerformanceProfiler
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(componentName, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    profiler.trackComponent(componentName, isAnimated)
                }
                Lifecycle.Event.ON_STOP -> {
                    // Component stopped, final tracking
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}