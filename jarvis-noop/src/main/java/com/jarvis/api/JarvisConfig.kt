package com.jarvis.api

/**
 * No-op configuration class for release builds.
 * Provides the same API but all settings are ignored.
 */
data class JarvisConfig(
    // Core Features - all ignored in no-op
    val enableShakeDetection: Boolean = false,
    val enableNetworkMonitoring: Boolean = false,
    val enablePreferencesInspection: Boolean = false,
    val enablePerformanceTracking: Boolean = false,
    val enableErrorTracking: Boolean = false,
    
    // UI Configuration - all ignored in no-op
    val enableTransparentBars: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val fabPosition: FabPosition = FabPosition.BOTTOM_END,
    
    // Network Settings - all ignored in no-op
    val networkLogRetentionDays: Int = 0,
    val maxNetworkLogSize: Int = 0,
    val captureRequestBodies: Boolean = false,
    val captureResponseBodies: Boolean = false,
    
    // Performance Settings - all ignored in no-op
    val performanceMetricsInterval: Long = 0L,
    val enableMemoryProfiling: Boolean = false,
    val enableCpuProfiling: Boolean = false,
    
    // Privacy Settings - all ignored in no-op
    val enableDataExport: Boolean = false,
    val enableRemoteLogging: Boolean = false,
    val anonymizeUserData: Boolean = true,
    
    // Debug Settings - all ignored in no-op
    val debugMode: Boolean = false,
    val logLevel: LogLevel = LogLevel.NONE
)

enum class ThemeMode {
    AUTO, LIGHT, DARK
}

enum class FabPosition {
    TOP_START, TOP_END, BOTTOM_START, BOTTOM_END
}

enum class LogLevel {
    NONE, ERROR, WARNING, INFO, DEBUG, VERBOSE
}