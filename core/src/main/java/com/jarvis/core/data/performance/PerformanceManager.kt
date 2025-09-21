package com.jarvis.core.data.performance

import android.app.Application
import com.jarvis.core.domain.performance.PerformanceConfig
import com.jarvis.core.domain.performance.PerformanceRepository
import com.jarvis.core.domain.performance.PerformanceSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the lifecycle and initialization of performance monitoring
 */
@Singleton
class PerformanceManager @Inject constructor(
    private val performanceRepository: PerformanceRepository
) {
    
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Initialize performance monitoring with default configuration
     */
    fun initialize() {
        managerScope.launch {
            try {
                // Ultra-conservative config to prevent frame drops
                val optimizedConfig = PerformanceConfig(
                    enableCpuMonitoring = true,      // Light CPU monitoring
                    enableMemoryMonitoring = true,   // Light memory monitoring  
                    enableFpsMonitoring = false,     // KEEP FPS DISABLED - was main culprit
                    enableModuleMonitoring = false,  // Disable complex module tracking
                    samplingIntervalMs = 10000,      // Very slow sampling (10 seconds)
                    maxHistorySize = 30,             // Small history (5 minutes)
                    enableBatteryMonitoring = false,
                    enableThermalMonitoring = false
                )
                
                android.util.Log.d("PerformanceManager", "Starting optimized performance monitoring")
                performanceRepository.startMonitoring(optimizedConfig)
            } catch (exception: Exception) {
                android.util.Log.e("PerformanceManager", "Failed to initialize performance monitoring", exception)
            }
        }
    }
    
    /**
     * Stop performance monitoring
     */
    fun shutdown() {
        managerScope.launch {
            try {
                performanceRepository.stopMonitoring()
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