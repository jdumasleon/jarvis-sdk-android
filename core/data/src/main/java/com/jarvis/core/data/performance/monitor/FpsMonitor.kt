package com.jarvis.core.data.performance.monitor

import android.os.Build
import android.view.Choreographer
import com.jarvis.core.domain.performance.FpsMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FpsMonitor @Inject constructor() {
    
    private val frameTimeHistory = ConcurrentLinkedQueue<Long>()
    private val maxHistorySize = 60 // Keep last 60 frames for calculations
    private var lastFrameTime = 0L
    private var isMonitoring = false
    private var frameCallback: Choreographer.FrameCallback? = null
    
    // Target frame time in nanoseconds (16.67ms for 60fps)
    private val targetFrameTimeNs = 16_666_667L
    
    fun getFpsMetricsFlow(intervalMs: Long = 1000): Flow<FpsMetrics> = flow {
        startMonitoring()
        
        try {
            var monitoringTime = 0L
            val maxMonitoringTime = 180_000L // 3 minutes max monitoring
            val safeIntervalMs = intervalMs.coerceAtLeast(1000) // Min 1s interval for stability
            
            while (monitoringTime < maxMonitoringTime && isMonitoring) {
                val metrics = calculateCurrentFpsMetrics()
                emit(metrics)
                kotlinx.coroutines.delay(safeIntervalMs)
                monitoringTime += safeIntervalMs
            }
        } catch (e: Exception) {
            emit(FpsMetrics(0f, 0f, 0f, 0f, 0, 0, 60f))
        } finally {
            stopMonitoring()
        }
    }.flowOn(Dispatchers.Main)
    
    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        lastFrameTime = System.nanoTime()
        
        // Start frame callback monitoring without suspending
        scheduleFrameCallback()
    }
    
    private fun scheduleFrameCallback() {
        if (frameCallback != null) return // Already have a callback scheduled
        
        val choreographer = Choreographer.getInstance()
        
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isMonitoring) {
                    // Stop the callback chain if monitoring is disabled
                    frameCallback = null
                    return
                }
                
                if (lastFrameTime > 0) {
                    val frameTime = frameTimeNanos - lastFrameTime
                    addFrameTime(frameTime)
                }
                lastFrameTime = frameTimeNanos
                
                // Schedule next frame if still monitoring
                if (isMonitoring) {
                    choreographer.postFrameCallback(this)
                }
            }
        }
        
        choreographer.postFrameCallback(frameCallback!!)
    }
    
    private fun addFrameTime(frameTimeNs: Long) {
        frameTimeHistory.offer(frameTimeNs)
        
        // Keep only the most recent frames
        while (frameTimeHistory.size > maxHistorySize) {
            frameTimeHistory.poll()
        }
    }
    
    private fun calculateCurrentFpsMetrics(): FpsMetrics {
        if (frameTimeHistory.isEmpty()) {
            return FpsMetrics(
                currentFps = 0f,
                averageFps = 0f,
                minFps = 0f,
                maxFps = 0f,
                frameDrops = 0,
                jankFrames = 0,
                refreshRate = getDisplayRefreshRate()
            )
        }
        
        val frameTimes = frameTimeHistory.toList()
        val frameCount = frameTimes.size
        
        // Calculate FPS from frame times
        val currentFps = if (frameTimes.isNotEmpty()) {
            1_000_000_000f / frameTimes.last()
        } else 0f
        
        val averageFrameTime = frameTimes.average()
        val averageFps = 1_000_000_000f / averageFrameTime.toFloat()
        
        val minFrameTime = frameTimes.minOrNull() ?: 0L
        val maxFrameTime = frameTimes.maxOrNull() ?: 0L
        val maxFps = if (minFrameTime > 0) 1_000_000_000f / minFrameTime else 0f
        val minFps = if (maxFrameTime > 0) 1_000_000_000f / maxFrameTime else 0f
        
        // Count dropped and jank frames
        val refreshRate = getDisplayRefreshRate()
        val targetFrameTime = 1_000_000_000f / refreshRate
        val jankThreshold = targetFrameTime * 1.5f // Frames taking 50% longer than target
        
        var frameDrops = 0
        var jankFrames = 0
        
        frameTimes.forEach { frameTime ->
            if (frameTime > targetFrameTime * 2) {
                frameDrops++
            }
            if (frameTime > jankThreshold) {
                jankFrames++
            }
        }
        
        return FpsMetrics(
            currentFps = currentFps.coerceAtMost(refreshRate),
            averageFps = averageFps.coerceAtMost(refreshRate),
            minFps = minFps.coerceAtMost(refreshRate),
            maxFps = maxFps.coerceAtMost(refreshRate),
            frameDrops = frameDrops,
            jankFrames = jankFrames,
            refreshRate = refreshRate
        )
    }
    
    private fun getDisplayRefreshRate(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Try to get actual refresh rate on Android 11+
                android.hardware.display.DisplayManager::class.java
                    .getDeclaredMethod("getDisplay", Int::class.java)
                    .invoke(null, 0)
                    ?.let { display ->
                        display::class.java
                            .getDeclaredMethod("getRefreshRate")
                            .invoke(display) as? Float
                    } ?: 60f
            } else {
                // Fallback for older versions
                60f
            }
        } catch (e: Exception) {
            60f // Default to 60fps
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        
        // Remove any pending frame callback
        frameCallback?.let { callback ->
            Choreographer.getInstance().removeFrameCallback(callback)
            frameCallback = null
        }
        
        frameTimeHistory.clear()
        lastFrameTime = 0L
    }
    
    /**
     * Get detailed frame timing information for debugging
     */
    fun getDetailedFrameInfo(): Map<String, Any> {
        val frameTimes = frameTimeHistory.toList()
        
        if (frameTimes.isEmpty()) {
            return mapOf("status" to "no_data")
        }
        
        val frameTimesMs = frameTimes.map { it / 1_000_000f } // Convert to milliseconds
        val refreshRate = getDisplayRefreshRate()
        val targetFrameTimeMs = 1000f / refreshRate
        
        return mapOf(
            "frame_count" to frameTimes.size,
            "avg_frame_time_ms" to frameTimesMs.average(),
            "min_frame_time_ms" to (frameTimesMs.minOrNull() ?: 0f),
            "max_frame_time_ms" to (frameTimesMs.maxOrNull() ?: 0f),
            "target_frame_time_ms" to targetFrameTimeMs,
            "refresh_rate_hz" to refreshRate,
            "frames_over_16ms" to frameTimesMs.count { it > 16.67f },
            "frames_over_33ms" to frameTimesMs.count { it > 33.33f },
            "frames_over_50ms" to frameTimesMs.count { it > 50f },
            "p95_frame_time_ms" to frameTimesMs.sorted().let { sorted ->
                if (sorted.isNotEmpty()) {
                    val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
                    sorted[index]
                } else 0f
            }
        )
    }
}