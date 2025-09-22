package com.jarvis.internal.feature.home.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting enhanced dashboard metrics with session filtering and advanced analytics
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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