package com.jarvis.core.internal.data.performance

import android.app.Application
import androidx.annotation.RestrictTo
import com.jarvis.core.internal.domain.performance.*
import com.jarvis.core.internal.domain.performance.MemoryMetrics
import com.jarvis.core.internal.domain.performance.PerformanceConfig
import com.jarvis.core.internal.domain.performance.PerformanceRepository
import com.jarvis.core.internal.data.performance.monitor.FpsMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the lifecycle and initialization of performance monitoring
 */
@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PerformanceManager @Inject constructor(
    private val performanceRepository: PerformanceRepository,
    private val fpsMonitor: FpsMonitor
) {

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _isCollecting = MutableStateFlow(false)
    val isCollecting: StateFlow<Boolean> = _isCollecting

    // Store the session snapshot when pausing (to preserve data when Jarvis opens)
    private val _sessionSnapshot = MutableStateFlow<PerformanceSnapshot?>(null)
    val sessionSnapshot: StateFlow<PerformanceSnapshot?> = _sessionSnapshot
    
    /**
     * Initialize performance monitoring with default configuration
     * Starts in collecting mode (monitoring host app)
     */
    fun initialize() {
        managerScope.launch {
            try {
                // Ultra-conservative config to prevent frame drops
                val optimizedConfig = PerformanceConfig(
                    enableCpuMonitoring = true,      // Light CPU monitoring
                    enableMemoryMonitoring = true,   // Light memory monitoring
                    enableFpsMonitoring = true,     // KEEP FPS DISABLED - was main culprit
                    enableModuleMonitoring = false,  // Disable complex module tracking
                    samplingIntervalMs = 10000,      // Very slow sampling (10 seconds)
                    maxHistorySize = 30,             // Small history (5 minutes)
                    enableBatteryMonitoring = true,
                    enableThermalMonitoring = false
                )

                android.util.Log.d("PerformanceManager", "Starting optimized performance monitoring")
                performanceRepository.startMonitoring(optimizedConfig)
                fpsMonitor.start()
                _isCollecting.value = true // Start collecting immediately
            } catch (exception: Exception) {
                android.util.Log.e("PerformanceManager", "Failed to initialize performance monitoring", exception)
            }
        }
    }

    /**
     * Pause performance data collection
     * Call this when Jarvis UI is opened to avoid measuring Jarvis's own performance
     * Captures and stores the accumulated session metrics before pausing
     */
    suspend fun pauseCollection() {
        try {
            android.util.Log.d("PerformanceManager", "Pausing performance collection (Jarvis opened)")

            // Capture the aggregated session data before pausing
            val history = performanceRepository.getPerformanceHistory(durationMinutes = 60).first()
            if (history.isNotEmpty()) {
                val aggregated = aggregatePerformanceHistory(history)
                _sessionSnapshot.value = aggregated
                android.util.Log.d("PerformanceManager", "Stored session snapshot with ${history.size} data points")
            } else {
                android.util.Log.w("PerformanceManager", "No history available to aggregate")
            }

            _isCollecting.value = false
            fpsMonitor.stop()
        } catch (exception: Exception) {
            android.util.Log.e("PerformanceManager", "Failed to pause collection", exception)
        }
    }

    /**
     * Aggregate performance history into a single snapshot
     */
    private fun aggregatePerformanceHistory(history: List<PerformanceSnapshot>): PerformanceSnapshot {
        // Aggregate CPU metrics
        val cpuSnapshots = history.mapNotNull { it.cpuUsage }
        val avgCpu = if (cpuSnapshots.isNotEmpty()) {
            CpuMetrics(
                cpuUsagePercent = cpuSnapshots.map { it.cpuUsagePercent }.average().toFloat(),
                appCpuUsagePercent = cpuSnapshots.map { it.appCpuUsagePercent }.average().toFloat(),
                systemCpuUsagePercent = cpuSnapshots.map { it.systemCpuUsagePercent }.average().toFloat(),
                cores = cpuSnapshots.first().cores,
                threadCount = cpuSnapshots.map { it.threadCount }.average().toInt()
            )
        } else null

        // Aggregate Memory metrics
        val memorySnapshots = history.mapNotNull { it.memoryUsage }
        val avgMemory = if (memorySnapshots.isNotEmpty()) {
            MemoryMetrics(
                heapUsedMB = memorySnapshots.map { it.heapUsedMB }.average().toFloat(),
                heapTotalMB = memorySnapshots.map { it.heapTotalMB }.average().toFloat(),
                heapMaxMB = memorySnapshots.map { it.heapMaxMB }.average().toFloat(),
                nativeHeapUsedMB = memorySnapshots.map { it.nativeHeapUsedMB }.average().toFloat(),
                nativeHeapTotalMB = memorySnapshots.map { it.nativeHeapTotalMB }.average().toFloat(),
                availableMemoryMB = memorySnapshots.map { it.availableMemoryMB }.average().toFloat(),
                totalMemoryMB = memorySnapshots.first().totalMemoryMB,
                memoryPressure = memorySnapshots.last().memoryPressure // Use latest pressure
            )
        } else null

        // Aggregate FPS metrics
        val fpsSnapshots = history.mapNotNull { it.fpsMetrics }
        val avgFps = if (fpsSnapshots.isNotEmpty()) {
            FpsMetrics(
                currentFps = fpsSnapshots.map { it.currentFps }.average().toFloat(),
                averageFps = fpsSnapshots.map { it.averageFps }.average().toFloat(),
                minFps = fpsSnapshots.minOf { it.minFps },
                maxFps = fpsSnapshots.maxOf { it.maxFps },
                frameDrops = fpsSnapshots.sumOf { it.frameDrops },
                jankFrames = fpsSnapshots.sumOf { it.jankFrames },
                refreshRate = fpsSnapshots.first().refreshRate
            )
        } else null

        // Aggregate Battery metrics
        val batterySnapshots = history.mapNotNull { it.batteryLevel }
        val avgBattery = if (batterySnapshots.isNotEmpty()) {
            batterySnapshots.average().toFloat()
        } else null

        return PerformanceSnapshot(
            timestamp = System.currentTimeMillis(),
            cpuUsage = avgCpu,
            memoryUsage = avgMemory,
            fpsMetrics = avgFps,
            batteryLevel = avgBattery,
            moduleMetrics = null
        )
    }

    /**
     * Resume performance data collection
     * Call this when Jarvis UI is closed to resume monitoring host app
     */
    fun resumeCollection() {
        managerScope.launch {
            try {
                android.util.Log.d("PerformanceManager", "Resuming performance collection (Jarvis closed)")
                _isCollecting.value = true
                fpsMonitor.start()
            } catch (exception: Exception) {
                android.util.Log.e("PerformanceManager", "Failed to resume collection", exception)
            }
        }
    }

    /**
     * Get the current session snapshot
     * Returns the aggregated metrics captured when Jarvis was last opened
     */
    fun getSessionSnapshot(): PerformanceSnapshot? {
        return _sessionSnapshot.value
    }
    
    /**
     * Stop performance monitoring
     */
    fun shutdown() {
        managerScope.launch {
            try {
                performanceRepository.stopMonitoring()
                fpsMonitor.stop()
            } catch (exception: Exception) {
                android.util.Log.e("PerformanceManager", "Failed to stop performance monitoring", exception)
            }
        }
    }
    
    /**
     * Check if monitoring is active
     */
    suspend fun isMonitoring(): Boolean {
        return try {
            performanceRepository.isMonitoring()
        } catch (exception: Exception) {
            false
        }
    }
    
    /**
     * Get performance metrics flow for real-time monitoring
     */
    fun getPerformanceMetricsFlow(): Flow<PerformanceSnapshot> {
        return performanceRepository.getPerformanceMetricsFlow()
    }
}