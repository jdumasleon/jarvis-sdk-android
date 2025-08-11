package com.jarvis.features.home.domain.usecase

import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get dashboard metrics
 */
class GetDashboardMetricsUseCase(
    private val repository: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardMetrics> {
        return repository.getDashboardMetrics()
    }
}