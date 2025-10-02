package com.jarvis.core.internal.data.performance.repository

import androidx.annotation.RestrictTo

import com.google.gson.Gson
import com.jarvis.core.internal.data.performance.monitor.CpuMonitor
import com.jarvis.core.internal.data.performance.monitor.FpsMonitor
import com.jarvis.core.internal.data.performance.monitor.JankMonitor
import com.jarvis.core.internal.data.performance.monitor.MemoryMonitor
import com.jarvis.core.internal.data.performance.monitor.ModuleLoadMonitor
import com.jarvis.core.internal.domain.performance.CpuMetrics
import com.jarvis.core.internal.domain.performance.FpsMetrics
import com.jarvis.core.internal.domain.performance.MemoryMetrics
import com.jarvis.core.internal.domain.performance.ModuleMetrics
import com.jarvis.core.internal.domain.performance.PerformanceConfig
import com.jarvis.core.internal.domain.performance.PerformanceRepository
import com.jarvis.core.internal.domain.performance.PerformanceSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PerformanceRepositoryImpl @Inject constructor(
    private val cpuMonitor: CpuMonitor,
    private val memoryMonitor: MemoryMonitor,
    private val fpsMonitor: FpsMonitor,
    private val moduleLoadMonitor: ModuleLoadMonitor,
    private val jankMonitor: JankMonitor,
    private val gson: Gson,
    @com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher 
    private val ioDispatcher: CoroutineDispatcher
) : PerformanceRepository {
    
    private val performanceHistory = ConcurrentLinkedQueue<PerformanceSnapshot>()
    private val _config = MutableStateFlow(PerformanceConfig())
    private val _isMonitoring = MutableStateFlow(false)
    
    override fun getPerformanceMetricsFlow(): Flow<PerformanceSnapshot> {
        return combine(
            getCpuMetricsFlow().flowOn(ioDispatcher),
            getMemoryMetricsFlow().flowOn(ioDispatcher),
            getFpsMetricsFlow(),
            getModuleMetricsFlow().flowOn(ioDispatcher)
        ) { cpu, memory, fps, modules ->
            val snapshot = PerformanceSnapshot(
                cpuUsage = cpu,
                memoryUsage = memory,
                fpsMetrics = fps,
                moduleMetrics = modules
            )
            
            addToHistory(snapshot)
            snapshot
        }
    }
    
    override fun getCpuMetricsFlow(): Flow<CpuMetrics> {
        return cpuMonitor.getCpuMetricsFlow(_config.value.samplingIntervalMs)
    }
    
    override fun getMemoryMetricsFlow(): Flow<MemoryMetrics> {
        return memoryMonitor.getMemoryMetricsFlow(_config.value.samplingIntervalMs)
    }
    
    override fun getFpsMetricsFlow(): Flow<FpsMetrics> {
        return fpsMonitor.getFpsMetricsFlow(_config.value.samplingIntervalMs)
    }
    
    override fun getModuleMetricsFlow(): Flow<ModuleMetrics> {
        return moduleLoadMonitor.moduleMetricsFlow
    }
    
    override fun getPerformanceHistory(durationMinutes: Int): Flow<List<PerformanceSnapshot>> = flow {
        val cutoffTime = System.currentTimeMillis() - (durationMinutes * 60 * 1000)
        val filteredHistory = performanceHistory.filter { it.timestamp >= cutoffTime }
        emit(filteredHistory)
    }
    
    override suspend fun startMonitoring(config: PerformanceConfig) {
        _config.value = config
        _isMonitoring.value = true
    }
    
    override suspend fun stopMonitoring() {
        _isMonitoring.value = false
        fpsMonitor.stopMonitoring()
    }
    
    override suspend fun isMonitoring(): Boolean {
        return _isMonitoring.value
    }
    
    override suspend fun updateConfig(config: PerformanceConfig) {
        _config.value = config
    }
    
    override suspend fun getConfig(): PerformanceConfig {
        return _config.value
    }
    
    override suspend fun clearHistory() {
        performanceHistory.clear()
        moduleLoadMonitor.clearHistory()
    }
    
    override suspend fun exportMetrics(): String {
        val exportData = mapOf(
            "config" to _config.value,
            "history" to performanceHistory.toList(),
            "detailed_cpu" to emptyMap<String, Any>(), // Could add detailed CPU info
            "detailed_memory" to memoryMonitor.getDetailedMemoryInfo(),
            "detailed_fps" to fpsMonitor.getDetailedFrameInfo(),
            "detailed_modules" to moduleLoadMonitor.getDetailedModuleStats(),
            "jarvis_assistant_jank" to jankMonitor.getDetailedReport(),
            "export_timestamp" to System.currentTimeMillis()
        )
        
        return gson.toJson(exportData)
    }
    
    /**
     * Get Jarvis Assistant specific jank monitoring flow
     */
    fun getJarvisAssistantJankFlow() = jankMonitor.startMonitoring()
    
    /**
     * Stop Jarvis Assistant jank monitoring
     */
    fun stopJarvisAssistantJankMonitoring() {
        jankMonitor.stopMonitoring()
    }
    
    private fun addToHistory(snapshot: PerformanceSnapshot) {
        performanceHistory.offer(snapshot)
        
        // Maintain history size limit
        val maxSize = _config.value.maxHistorySize
        while (performanceHistory.size > maxSize) {
            performanceHistory.poll()
        }
    }
}