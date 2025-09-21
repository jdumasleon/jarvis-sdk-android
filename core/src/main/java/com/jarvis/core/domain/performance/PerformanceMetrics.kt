package com.jarvis.core.domain.performance

import kotlin.time.Duration

/**
 * Core performance metrics data classes for cross-application monitoring
 */
data class PerformanceSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val cpuUsage: CpuMetrics? = null,
    val memoryUsage: MemoryMetrics? = null,
    val fpsMetrics: FpsMetrics? = null,
    val moduleMetrics: ModuleMetrics? = null,
    val batteryLevel: Float? = null,
    val thermalState: ThermalState = ThermalState.NORMAL
)

data class CpuMetrics(
    val cpuUsagePercent: Float, // 0.0 to 100.0
    val appCpuUsagePercent: Float, // App-specific CPU usage
    val systemCpuUsagePercent: Float, // System-wide CPU usage
    val cores: Int = Runtime.getRuntime().availableProcessors(),
    val threadCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class MemoryMetrics(
    val heapUsedMB: Float,
    val heapTotalMB: Float,
    val heapMaxMB: Float,
    val nativeHeapUsedMB: Float,
    val nativeHeapTotalMB: Float,
    val availableMemoryMB: Float,
    val totalMemoryMB: Float,
    val memoryPressure: MemoryPressure = MemoryPressure.LOW,
    val timestamp: Long = System.currentTimeMillis()
) {
    val heapUsagePercent: Float
        get() = if (heapMaxMB > 0) (heapUsedMB / heapMaxMB) * 100f else 0f
        
    val nativeHeapUsagePercent: Float
        get() = if (nativeHeapTotalMB > 0) (nativeHeapUsedMB / nativeHeapTotalMB) * 100f else 0f
}

data class FpsMetrics(
    val currentFps: Float,
    val averageFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val frameDrops: Int,
    val jankFrames: Int, // Frames that took >16.67ms (60fps threshold)
    val refreshRate: Float = 60f,
    val timestamp: Long = System.currentTimeMillis()
) {
    val fpsStability: FpsStability
        get() = when {
            currentFps >= refreshRate * 0.95f -> FpsStability.EXCELLENT
            currentFps >= refreshRate * 0.85f -> FpsStability.GOOD
            currentFps >= refreshRate * 0.70f -> FpsStability.FAIR
            else -> FpsStability.POOR
        }
}

data class ModuleMetrics(
    val moduleLoadTimes: List<ModuleLoadTime>,
    val startupTime: Duration? = null,
    val coldStartTime: Duration? = null,
    val warmStartTime: Duration? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ModuleLoadTime(
    val moduleName: String,
    val loadDuration: Duration,
    val loadType: LoadType,
    val isLazyLoaded: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MemoryPressure {
    LOW, MODERATE, HIGH, CRITICAL
}

enum class ThermalState {
    NORMAL, FAIR, SERIOUS, CRITICAL, EMERGENCY, SHUTDOWN
}

enum class FpsStability {
    EXCELLENT, GOOD, FAIR, POOR
}

enum class LoadType {
    CLASS_LOADING,
    DAGGER_MODULE,
    COMPOSE_MODULE,
    LIBRARY_INIT,
    DATABASE_INIT,
    NETWORK_INIT,
    CUSTOM
}

/**
 * Performance monitoring configuration
 */
data class PerformanceConfig(
    val enableCpuMonitoring: Boolean = true,
    val enableMemoryMonitoring: Boolean = true,
    val enableFpsMonitoring: Boolean = true,
    val enableModuleMonitoring: Boolean = true,
    val samplingIntervalMs: Long = 1000, // 1 second default
    val maxHistorySize: Int = 300, // 5 minutes at 1-second intervals
    val enableBatteryMonitoring: Boolean = false,
    val enableThermalMonitoring: Boolean = false
)

/**
 * Performance alert thresholds
 */
data class PerformanceThresholds(
    val cpuThreshold: Float = 80f, // Percent
    val memoryThreshold: Float = 85f, // Percent
    val fpsThreshold: Float = 45f, // FPS
    val frameDropThreshold: Int = 10, // Dropped frames per second
    val moduleLoadThreshold: Duration = Duration.parse("PT2S") // 2 seconds
)

// Mock objects for testing and previews
object PerformanceSnapshotMock {
    val mockPerformanceSnapshot: PerformanceSnapshot
        get() = PerformanceSnapshot(
        timestamp = System.currentTimeMillis(),
        cpuUsage = CpuMetrics(
            cpuUsagePercent = 23.5f,
            appCpuUsagePercent = 8.2f,
            systemCpuUsagePercent = 15.3f,
            cores = 8,
            threadCount = 45,
            timestamp = System.currentTimeMillis()
        ),
        memoryUsage = MemoryMetrics(
            heapUsedMB = 156.8f,
            heapTotalMB = 512.0f,
            heapMaxMB = 1024.0f,
            nativeHeapUsedMB = 89.2f,
            nativeHeapTotalMB = 256.0f,
            availableMemoryMB = 2048.0f,
            totalMemoryMB = 8192.0f,
            memoryPressure = MemoryPressure.LOW,
            timestamp = System.currentTimeMillis()
        ),
        fpsMetrics = FpsMetrics(
            currentFps = 58.3f,
            averageFps = 56.7f,
            minFps = 45.2f,
            maxFps = 60.0f,
            frameDrops = 3,
            jankFrames = 12,
            refreshRate = 60f,
            timestamp = System.currentTimeMillis()
        ),
        moduleMetrics = ModuleMetrics(
            moduleLoadTimes = listOf(
                ModuleLoadTime("HomeModule", Duration.parse("PT0.234S"), LoadType.DAGGER_MODULE),
                ModuleLoadTime("NetworkModule", Duration.parse("PT0.156S"), LoadType.NETWORK_INIT),
                ModuleLoadTime("DatabaseModule", Duration.parse("PT0.089S"), LoadType.DATABASE_INIT)
            ),
            startupTime = Duration.parse("PT1.2S"),
            coldStartTime = Duration.parse("PT2.8S"),
            warmStartTime = Duration.parse("PT0.6S"),
            timestamp = System.currentTimeMillis()
        ),
        batteryLevel = 68.5f,
        thermalState = ThermalState.NORMAL
    )
}