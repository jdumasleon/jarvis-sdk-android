package com.jarvis.features.home.domain.usecase

import com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.features.home.domain.entity.SessionFilter
import com.jarvis.features.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting enhanced dashboard metrics with session filtering and advanced analytics
 */
class GetEnhancedDashboardMetricsUseCase(
    private val repository: DashboardRepository
) {
    /**
     * Get enhanced dashboard metrics with session filtering
     */
    operator fun invoke(sessionFilter: SessionFilter = SessionFilter.LAST_SESSION): Flow<EnhancedDashboardMetrics> {
        return repository.getEnhancedDashboardMetrics(sessionFilter)
    }
}