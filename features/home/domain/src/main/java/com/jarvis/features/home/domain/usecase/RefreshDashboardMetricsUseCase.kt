package com.jarvis.features.home.domain.usecase

import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.repository.DashboardRepository

/**
 * Use case to refresh dashboard metrics
 */
class RefreshDashboardMetricsUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): DashboardMetrics {
        return repository.refreshMetrics()
    }
}