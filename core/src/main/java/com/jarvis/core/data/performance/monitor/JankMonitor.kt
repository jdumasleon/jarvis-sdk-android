package com.jarvis.core.data.performance.monitor

import android.os.Build
import android.util.Log
import android.view.Choreographer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Specialized jank monitor for DSJarvisAssistant component performance
 * Tracks frame drops, animation performance, and UI freeze detection
 */
@Singleton
class JankMonitor @Inject constructor() {

    data class JankMetrics(
        val frameDrops: Int = 0,
        val jankFrames: Int = 0,
        val frozenFrames: Int = 0,
        val averageFrameTime: Float = 16.67f, // ms
        val maxFrameTime: Float = 16.67f,     // ms
        val animationFrameRate: Float = 60f,
        val isAnimationSmooth: Boolean = true,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val jankSeverity: JankSeverity
            get() = when {
                frozenFrames > 0 -> JankSeverity.CRITICAL
                jankFrames > 10 -> JankSeverity.HIGH
                jankFrames > 5 -> JankSeverity.MEDIUM
                frameDrops > 0 -> JankSeverity.LOW
                else -> JankSeverity.NONE
            }
    }

    enum class JankSeverity {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

    private val frameTimeHistory = ConcurrentLinkedQueue<Long>()
    private val maxHistorySize = 120 // 2 seconds at 60fps
    private val isMonitoring = AtomicBoolean(false)
    private val lastFrameTime = AtomicLong(0L)
    private var frameCallback: Choreographer.FrameCallback? = null
    private var monitoringJob: Job? = null

    // Frame time thresholds (in nanoseconds)
    private val targetFrameTimeNs = 16_666_667L // 16.67ms for 60fps
    private val jankThresholdNs = targetFrameTimeNs + (targetFrameTimeNs / 2) // 25ms
    private val frozenThresholdNs = targetFrameTimeNs * 4 // 67ms (4 frames)

    companion object {
        private const val TAG = "JarvisJankMonitor"
        private const val MONITORING_DURATION_MS = 30_000L // Monitor for 30 seconds max
        private const val SAMPLE_INTERVAL_MS = 2_000L // Report every 2 seconds
    }

    /**
     * Start monitoring DSJarvisAssistant performance
     */
    fun startMonitoring(): Flow<JankMetrics> = flow {
        if (isMonitoring.compareAndSet(false, true)) {
            Log.d(TAG, "Starting DSJarvisAssistant jank monitoring")
            
            try {
                setupFrameCallback()
                var monitoringTime = 0L
                
                while (monitoringTime < MONITORING_DURATION_MS && isMonitoring.get()) {
                    delay(SAMPLE_INTERVAL_MS)
                    monitoringTime += SAMPLE_INTERVAL_MS
                    
                    val metrics = calculateJankMetrics()
                    emit(metrics)
                    
                    // Log critical jank issues
                    if (metrics.jankSeverity >= JankSeverity.HIGH) {
                        Log.w(TAG, "DSJarvisAssistant jank detected: ${metrics.jankSeverity}, " +
                                "Frozen: ${metrics.frozenFrames}, Jank: ${metrics.jankFrames}, " +
                                "MaxFrameTime: ${metrics.maxFrameTime}ms")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during jank monitoring", e)
                emit(JankMetrics()) // Emit default metrics on error
            } finally {
                stopMonitoring()
            }
        }
    }.flowOn(Dispatchers.Main)

    private fun setupFrameCallback() {
        if (frameCallback != null) return
        
        val choreographer = Choreographer.getInstance()
        lastFrameTime.set(System.nanoTime())
        
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!isMonitoring.get()) {
                    frameCallback = null
                    return
                }
                
                val lastTime = lastFrameTime.get()
                if (lastTime > 0) {
                    val frameTime = frameTimeNanos - lastTime
                    addFrameTime(frameTime)
                }
                lastFrameTime.set(frameTimeNanos)
                
                // Schedule next frame
                if (isMonitoring.get()) {
                    choreographer.postFrameCallback(this)
                }
            }
        }
        
        choreographer.postFrameCallback(frameCallback!!)
    }

    private fun addFrameTime(frameTimeNs: Long) {
        frameTimeHistory.offer(frameTimeNs)
        
        // Maintain history size
        while (frameTimeHistory.size > maxHistorySize) {
            frameTimeHistory.poll()
        }
    }

    private fun calculateJankMetrics(): JankMetrics {
        val frameTimes = frameTimeHistory.toList()
        if (frameTimes.isEmpty()) {
            return JankMetrics()
        }

        var frameDrops = 0
        var jankFrames = 0
        var frozenFrames = 0
        
        val frameTimesMs = frameTimes.map { it / 1_000_000f } // Convert to milliseconds
        val averageFrameTime = frameTimesMs.average().toFloat()
        val maxFrameTime = frameTimesMs.maxOrNull() ?: 16.67f
        
        // Count different types of frame issues
        frameTimes.forEach { frameTimeNs ->
            when {
                frameTimeNs > frozenThresholdNs -> {
                    frozenFrames++
                    jankFrames++ // Frozen frames are also jank
                    frameDrops++ // And also dropped
                }
                frameTimeNs > jankThresholdNs -> {
                    jankFrames++
                    if (frameTimeNs > targetFrameTimeNs * 2) {
                        frameDrops++
                    }
                }
                frameTimeNs > targetFrameTimeNs * 1.2 -> {
                    // Minor frame drops (20% over target)
                    frameDrops++
                }
            }
        }

        // Calculate current animation frame rate
        val currentFps = if (averageFrameTime > 0) 1000f / averageFrameTime else 60f
        val isSmooth = jankFrames <= 2 && frozenFrames == 0 && currentFps >= 50f

        return JankMetrics(
            frameDrops = frameDrops,
            jankFrames = jankFrames,
            frozenFrames = frozenFrames,
            averageFrameTime = averageFrameTime,
            maxFrameTime = maxFrameTime,
            animationFrameRate = currentFps.coerceAtMost(getDisplayRefreshRate()),
            isAnimationSmooth = isSmooth
        )
    }

    private fun getDisplayRefreshRate(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                60f // Simplified for now
            } else {
                60f
            }
        } catch (e: Exception) {
            60f
        }
    }

    /**
     * Stop monitoring and clean up resources
     */
    fun stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            Log.d(TAG, "Stopping DSJarvisAssistant jank monitoring")
            
            // Cancel monitoring job
            monitoringJob?.cancel()
            
            // Remove frame callback
            frameCallback?.let { callback ->
                Choreographer.getInstance().removeFrameCallback(callback)
                frameCallback = null
            }
            
            // Clear data
            frameTimeHistory.clear()
            lastFrameTime.set(0L)
        }
    }

    /**
     * Get current monitoring state
     */
    fun isCurrentlyMonitoring(): Boolean = isMonitoring.get()

    /**
     * Get detailed performance report for debugging
     */
    fun getDetailedReport(): Map<String, Any> {
        val frameTimes = frameTimeHistory.toList()
        if (frameTimes.isEmpty()) {
            return mapOf("status" to "no_data")
        }

        val frameTimesMs = frameTimes.map { it / 1_000_000f }
        val p95FrameTime = frameTimesMs.sorted().let { sorted ->
            if (sorted.isNotEmpty()) {
                val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
                sorted[index]
            } else 0f
        }

        return mapOf(
            "component" to "DSJarvisAssistant",
            "monitoring_active" to isMonitoring.get(),
            "frame_count" to frameTimes.size,
            "avg_frame_time_ms" to frameTimesMs.average(),
            "min_frame_time_ms" to (frameTimesMs.minOrNull() ?: 0f),
            "max_frame_time_ms" to (frameTimesMs.maxOrNull() ?: 0f),
            "p95_frame_time_ms" to p95FrameTime,
            "target_frame_time_ms" to (targetFrameTimeNs / 1_000_000f),
            "jank_threshold_ms" to (jankThresholdNs / 1_000_000f),
            "frozen_threshold_ms" to (frozenThresholdNs / 1_000_000f),
            "frames_over_16ms" to frameTimesMs.count { it > 16.67f },
            "frames_over_25ms" to frameTimesMs.count { it > 25f },
            "frames_over_67ms" to frameTimesMs.count { it > 67f },
            "recommended_action" to when (calculateJankMetrics().jankSeverity) {
                JankSeverity.CRITICAL -> "Disable animations immediately"
                JankSeverity.HIGH -> "Reduce animation complexity"
                JankSeverity.MEDIUM -> "Consider reducing animation frequency"
                JankSeverity.LOW -> "Monitor closely"
                JankSeverity.NONE -> "Performance is good"
            }
        )
    }
}