package com.jarvis.core.internal.data.performance.monitor

import androidx.annotation.RestrictTo

import android.os.SystemClock
import com.jarvis.core.internal.domain.performance.LoadType
import com.jarvis.core.internal.domain.performance.ModuleLoadTime
import com.jarvis.core.internal.domain.performance.ModuleMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ModuleLoadMonitor @Inject constructor() {
    
    private val moduleLoadTimes = mutableListOf<ModuleLoadTime>()
    private val activeTimers = ConcurrentHashMap<String, Long>()
    private val _moduleMetricsFlow = MutableStateFlow(
        ModuleMetrics(
            moduleLoadTimes = emptyList(),
            startupTime = null,
            coldStartTime = null,
            warmStartTime = null
        )
    )
    
    val moduleMetricsFlow: Flow<ModuleMetrics> = _moduleMetricsFlow.asStateFlow()
    
    // App startup timing
    private var appStartTime: Long = 0
    private var coldStartTime: Long = 0
    private var warmStartTime: Long = 0
    
    init {
        // Record app start time
        appStartTime = SystemClock.elapsedRealtime()
    }
    
    /**
     * Start timing a module load operation
     */
    fun startModuleLoad(moduleName: String, loadType: LoadType = LoadType.CUSTOM): String {
        val timerId = "${moduleName}_${System.nanoTime()}"
        activeTimers[timerId] = SystemClock.elapsedRealtimeNanos()
        return timerId
    }
    
    /**
     * End timing a module load operation
     */
    fun endModuleLoad(
        timerId: String,
        moduleName: String,
        loadType: LoadType = LoadType.CUSTOM,
        isLazyLoaded: Boolean = false
    ) {
        val startTime = activeTimers.remove(timerId) ?: return
        val endTime = SystemClock.elapsedRealtimeNanos()
        val duration = (endTime - startTime).milliseconds
        
        val moduleLoadTime = ModuleLoadTime(
            moduleName = moduleName,
            loadDuration = duration,
            loadType = loadType,
            isLazyLoaded = isLazyLoaded
        )
        
        synchronized(moduleLoadTimes) {
            moduleLoadTimes.add(moduleLoadTime)
            // Keep only recent load times (last 100)
            if (moduleLoadTimes.size > 100) {
                moduleLoadTimes.removeAt(0)
            }
        }
        
        updateMetricsFlow()
    }
    
    /**
     * Record a module load time directly
     */
    fun recordModuleLoad(
        moduleName: String,
        duration: Duration,
        loadType: LoadType = LoadType.CUSTOM,
        isLazyLoaded: Boolean = false
    ) {
        val moduleLoadTime = ModuleLoadTime(
            moduleName = moduleName,
            loadDuration = duration,
            loadType = loadType,
            isLazyLoaded = isLazyLoaded
        )
        
        synchronized(moduleLoadTimes) {
            moduleLoadTimes.add(moduleLoadTime)
            if (moduleLoadTimes.size > 100) {
                moduleLoadTimes.removeAt(0)
            }
        }
        
        updateMetricsFlow()
    }
    
    /**
     * Record cold start completion
     */
    fun recordColdStart() {
        coldStartTime = SystemClock.elapsedRealtime() - appStartTime
        updateMetricsFlow()
    }
    
    /**
     * Record warm start completion
     */
    fun recordWarmStart() {
        warmStartTime = SystemClock.elapsedRealtime() - appStartTime
        updateMetricsFlow()
    }
    
    /**
     * Record general startup completion
     */
    fun recordStartupComplete() {
        val startupDuration = SystemClock.elapsedRealtime() - appStartTime
        updateMetricsFlow(startupDuration.milliseconds)
    }
    
    private fun updateMetricsFlow(startupTime: Duration? = null) {
        val metrics = ModuleMetrics(
            moduleLoadTimes = synchronized(moduleLoadTimes) { moduleLoadTimes.toList() },
            startupTime = startupTime ?: _moduleMetricsFlow.value.startupTime,
            coldStartTime = if (coldStartTime > 0) coldStartTime.milliseconds else null,
            warmStartTime = if (warmStartTime > 0) warmStartTime.milliseconds else null
        )
        
        _moduleMetricsFlow.value = metrics
    }
    
    /**
     * Get detailed module load statistics
     */
    fun getDetailedModuleStats(): Map<String, Any> {
        val loadTimes = synchronized(moduleLoadTimes) { moduleLoadTimes.toList() }
        
        if (loadTimes.isEmpty()) {
            return mapOf("status" to "no_data")
        }
        
        val durations = loadTimes.map { it.loadDuration.toDouble(DurationUnit.MILLISECONDS) }
        val groupedByType = loadTimes.groupBy { it.loadType }
        val groupedByModule = loadTimes.groupBy { it.moduleName }
        
        return mapOf(
            "total_modules_loaded" to loadTimes.size,
            "average_load_time_ms" to durations.average(),
            "min_load_time_ms" to (durations.minOrNull() ?: 0.0),
            "max_load_time_ms" to (durations.maxOrNull() ?: 0.0),
            "p95_load_time_ms" to durations.sorted().let { sorted ->
                if (sorted.isNotEmpty()) {
                    val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
                    sorted[index]
                } else 0.0
            },
            "modules_over_1s" to durations.count { it > 1000.0 },
            "modules_over_2s" to durations.count { it > 2000.0 },
            "load_types" to groupedByType.mapValues { (_, modules) ->
                mapOf(
                    "count" to modules.size,
                    "avg_time_ms" to modules.map { it.loadDuration.toDouble(DurationUnit.MILLISECONDS) }.average()
                )
            },
            "slowest_modules" to groupedByModule.mapValues { (_, modules) ->
                modules.maxByOrNull { it.loadDuration }?.loadDuration?.toDouble(DurationUnit.MILLISECONDS) ?: 0.0
            }.toList().sortedByDescending { it.second }.take(10),
            "lazy_loaded_count" to loadTimes.count { it.isLazyLoaded },
            "startup_time_ms" to (_moduleMetricsFlow.value.startupTime?.toDouble(DurationUnit.MILLISECONDS) ?: 0.0),
            "cold_start_time_ms" to (_moduleMetricsFlow.value.coldStartTime?.toDouble(DurationUnit.MILLISECONDS) ?: 0.0),
            "warm_start_time_ms" to (_moduleMetricsFlow.value.warmStartTime?.toDouble(DurationUnit.MILLISECONDS) ?: 0.0)
        )
    }
    
    /**
     * Clear all recorded module load times
     */
    fun clearHistory() {
        synchronized(moduleLoadTimes) {
            moduleLoadTimes.clear()
        }
        activeTimers.clear()
        updateMetricsFlow()
    }
    
    /**
     * Helper function to time a block of code
     */
    inline fun <T> timeModuleLoad(
        moduleName: String,
        loadType: LoadType = LoadType.CUSTOM,
        isLazyLoaded: Boolean = false,
        block: () -> T
    ): T {
        val timerId = startModuleLoad(moduleName, loadType)
        return try {
            block()
        } finally {
            endModuleLoad(timerId, moduleName, loadType, isLazyLoaded)
        }
    }
    
    /**
     * Helper function to time suspending functions
     */
    suspend inline fun <T> timeModuleLoadSuspend(
        moduleName: String,
        loadType: LoadType = LoadType.CUSTOM,
        isLazyLoaded: Boolean = false,
        block: suspend () -> T
    ): T {
        val timerId = startModuleLoad(moduleName, loadType)
        return try {
            block()
        } finally {
            endModuleLoad(timerId, moduleName, loadType, isLazyLoaded)
        }
    }
}