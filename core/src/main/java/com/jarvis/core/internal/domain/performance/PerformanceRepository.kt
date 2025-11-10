@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.domain.performance

import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.Flow

interface PerformanceRepository {
    
    /**
     * Get real-time performance metrics stream
     */
    fun getPerformanceMetricsFlow(): Flow<PerformanceSnapshot>
    
    /**
     * Get specific metric streams
     */
    fun getCpuMetricsFlow(): Flow<CpuMetrics>
    fun getMemoryMetricsFlow(): Flow<MemoryMetrics>

    fun getModuleMetricsFlow(): Flow<ModuleMetrics>
    fun getBatteryLevelFlow(): Flow<Float>
    
    /**
     * Historical data
     */
    fun getPerformanceHistory(durationMinutes: Int = 5): Flow<List<PerformanceSnapshot>>
    
    /**
     * Control monitoring
     */
    suspend fun startMonitoring(config: PerformanceConfig = PerformanceConfig())
    suspend fun stopMonitoring()
    suspend fun isMonitoring(): Boolean
    
    /**
     * Configuration
     */
    suspend fun updateConfig(config: PerformanceConfig)
    suspend fun getConfig(): PerformanceConfig
    
    /**
     * Data management
     */
    suspend fun clearHistory()
    suspend fun exportMetrics(): String
}