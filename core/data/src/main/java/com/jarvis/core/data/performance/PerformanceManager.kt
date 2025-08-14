package com.jarvis.core.data.performance

import android.app.Application
import com.jarvis.core.domain.performance.PerformanceConfig
import com.jarvis.core.domain.performance.PerformanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
                val defaultConfig = PerformanceConfig(
                    enableCpuMonitoring = true,
                    enableMemoryMonitoring = true,
                    enableFpsMonitoring = true,
                    enableModuleMonitoring = true,
                    samplingIntervalMs = 2000, // 2 seconds for better UX
                    maxHistorySize = 150, // 5 minutes at 2-second intervals
                    enableBatteryMonitoring = false,
                    enableThermalMonitoring = false
                )
                
                // Start monitoring with default config
                performanceRepository.startMonitoring(defaultConfig)
            } catch (exception: Exception) {
                // Log error but don't crash the app
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
}