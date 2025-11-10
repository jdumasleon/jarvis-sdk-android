@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.domain.performance

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.data.performance.PerformanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPerformanceMetricsUseCase @Inject constructor(
    private val repository: PerformanceRepository,
    private val performanceManager: PerformanceManager
) {
    /**
     * Get live performance metrics flow
     * Only emits when collection is active (not paused)
     */
    operator fun invoke(): Flow<PerformanceSnapshot> {
        return repository.getPerformanceMetricsFlow()
    }

    /**
     * Get the stored session snapshot
     * Returns the aggregated metrics from the host app session (excluding Jarvis's own performance)
     * This snapshot is captured and stored when Jarvis is opened
     */
    fun getSessionSnapshot(): PerformanceSnapshot? {
        return performanceManager.getSessionSnapshot()
    }
}

class GetPerformanceHistoryUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    operator fun invoke(durationMinutes: Int = 5): Flow<List<PerformanceSnapshot>> {
        return repository.getPerformanceHistory(durationMinutes)
    }
}

class StartPerformanceMonitoringUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    suspend operator fun invoke(config: PerformanceConfig = PerformanceConfig()) {
        repository.startMonitoring(config)
    }
}

class StopPerformanceMonitoringUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    suspend operator fun invoke() {
        repository.stopMonitoring()
    }
}

class UpdatePerformanceConfigUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    suspend operator fun invoke(config: PerformanceConfig) {
        repository.updateConfig(config)
    }
}

class ExportPerformanceMetricsUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    suspend operator fun invoke(): String {
        return repository.exportMetrics()
    }
}

/**
 * Pause performance data collection
 * Use when Jarvis UI is opened to avoid measuring Jarvis's own performance
 */
class PausePerformanceCollectionUseCase @Inject constructor(
    private val performanceManager: PerformanceManager
) {
    suspend operator fun invoke() {
        performanceManager.pauseCollection()
    }
}

/**
 * Resume performance data collection
 * Use when Jarvis UI is closed to resume monitoring host app
 */
class ResumePerformanceCollectionUseCase @Inject constructor(
    private val performanceManager: PerformanceManager
) {
    operator fun invoke() {
        performanceManager.resumeCollection()
    }
}