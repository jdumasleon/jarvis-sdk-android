@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.domain.performance

import androidx.annotation.RestrictTo

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPerformanceMetricsUseCase @Inject constructor(
    private val repository: PerformanceRepository
) {
    operator fun invoke(): Flow<PerformanceSnapshot> {
        return repository.getPerformanceMetricsFlow()
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