package com.jarvis.core.internal.data.performance.monitor

import androidx.annotation.RestrictTo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import com.jarvis.core.internal.domain.performance.MemoryMetrics
import com.jarvis.core.internal.domain.performance.MemoryPressure
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class MemoryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val activityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }
    
    fun getMemoryMetricsFlow(intervalMs: Long = 1000): Flow<MemoryMetrics> = flow {
        // ✅ CRITICAL FIX: Add timeout and caching to prevent excessive memory operations
        var lastMetrics: MemoryMetrics? = null
        var lastUpdate = 0L
        val cacheValidityMs = intervalMs / 2 // Cache for half the interval
        var consecutiveErrors = 0
        val maxErrors = 3
        
        while (consecutiveErrors < maxErrors) {
            try {
                val currentTime = System.currentTimeMillis()
                
                // Use cached metrics if still valid
                if (lastMetrics != null && (currentTime - lastUpdate) < cacheValidityMs) {
                    emit(lastMetrics!!)
                } else {
                    val metrics = getCurrentMemoryMetrics()
                    lastMetrics = metrics
                    lastUpdate = currentTime
                    emit(metrics)
                    consecutiveErrors = 0
                }
                
                kotlinx.coroutines.delay(intervalMs.coerceAtLeast(1000)) // Min 1s for memory checks
            } catch (e: Exception) {
                consecutiveErrors++
                // Emit basic metrics on error
                emit(MemoryMetrics(0f, 0f, 0f, 0f, 0f, 0f, 0f, MemoryPressure.LOW))
                kotlinx.coroutines.delay(intervalMs * 2)
            }
        }
    }.flowOn(Dispatchers.IO)
    
    private fun getCurrentMemoryMetrics(): MemoryMetrics {
        val runtime = Runtime.getRuntime()
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // Java heap memory
        val heapUsedMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        val heapTotalMB = runtime.totalMemory() / (1024f * 1024f)
        val heapMaxMB = runtime.maxMemory() / (1024f * 1024f)
        
        // Native heap memory
        val nativeHeapInfo = getNativeHeapInfo()
        
        // System memory
        val availableMemoryMB = memoryInfo.availMem / (1024f * 1024f)
        val totalMemoryMB = memoryInfo.totalMem / (1024f * 1024f)
        
        val memoryPressure = calculateMemoryPressure(memoryInfo, heapUsedMB, heapMaxMB)
        
        return MemoryMetrics(
            heapUsedMB = heapUsedMB,
            heapTotalMB = heapTotalMB,
            heapMaxMB = heapMaxMB,
            nativeHeapUsedMB = nativeHeapInfo.first,
            nativeHeapTotalMB = nativeHeapInfo.second,
            availableMemoryMB = availableMemoryMB,
            totalMemoryMB = totalMemoryMB,
            memoryPressure = memoryPressure
        )
    }
    
    private fun getNativeHeapInfo(): Pair<Float, Float> {
        return try {
            val nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize() / (1024f * 1024f)
            val nativeHeapSize = Debug.getNativeHeapSize() / (1024f * 1024f)
            Pair(nativeHeapAllocatedSize, nativeHeapSize)
        } catch (e: Exception) {
            Pair(0f, 0f)
        }
    }
    
    private fun getTotalMemoryLegacy(): Long {
        return try {
            val reader = java.io.BufferedReader(java.io.FileReader("/proc/meminfo"))
            val line = reader.readLine()
            reader.close()
            
            val parts = line.split("\\s+".toRegex())
            if (parts.size >= 2) {
                parts[1].toLong() * 1024 // Convert from KB to bytes
            } else {
                2L * 1024 * 1024 * 1024 // 2GB fallback
            }
        } catch (e: Exception) {
            2L * 1024 * 1024 * 1024 // 2GB fallback
        }
    }
    
    private fun calculateMemoryPressure(
        memoryInfo: ActivityManager.MemoryInfo,
        heapUsedMB: Float,
        heapMaxMB: Float
    ): MemoryPressure {
        val heapUsagePercent = if (heapMaxMB > 0) (heapUsedMB / heapMaxMB) * 100f else 0f
        val systemMemoryUsagePercent = ((memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem.toFloat()) * 100f
        
        return when {
            // Critical: Low memory threshold triggered OR heap usage > 90%
            memoryInfo.lowMemory || heapUsagePercent > 90f -> MemoryPressure.CRITICAL
            
            // High: System memory > 85% OR heap usage > 80%
            systemMemoryUsagePercent > 85f || heapUsagePercent > 80f -> MemoryPressure.HIGH
            
            // Moderate: System memory > 70% OR heap usage > 60%
            systemMemoryUsagePercent > 70f || heapUsagePercent > 60f -> MemoryPressure.MODERATE
            
            // Low: Normal operation
            else -> MemoryPressure.LOW
        }
    }
    
    /**
     * Get detailed memory breakdown for debugging
     */
    fun getDetailedMemoryInfo(): Map<String, Any> {
        val runtime = Runtime.getRuntime()
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val debugMemoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugMemoryInfo)
        
        return mapOf(
            "heap_used_mb" to (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f),
            "heap_total_mb" to runtime.totalMemory() / (1024f * 1024f),
            "heap_max_mb" to runtime.maxMemory() / (1024f * 1024f),
            "native_heap_mb" to Debug.getNativeHeapAllocatedSize() / (1024f * 1024f),
            "native_heap_total_mb" to Debug.getNativeHeapSize() / (1024f * 1024f),
            "system_available_mb" to memoryInfo.availMem / (1024f * 1024f),
            "system_total_mb" to memoryInfo.totalMem / (1024f * 1024f),
            "low_memory_threshold" to memoryInfo.threshold / (1024f * 1024f),
            "is_low_memory" to memoryInfo.lowMemory,
            "pss_mb" to debugMemoryInfo.totalPss / 1024f,
            "private_dirty_mb" to debugMemoryInfo.totalPrivateDirty / 1024f,
            "shared_dirty_mb" to debugMemoryInfo.totalSharedDirty / 1024f
        )
    }
}