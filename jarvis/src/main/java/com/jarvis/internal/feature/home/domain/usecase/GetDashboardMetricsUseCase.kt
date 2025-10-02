package com.jarvis.internal.feature.home.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.home.domain.entity.DashboardMetrics
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get dashboard metrics
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GetDashboardMetricsUseCase(
    private val repository: DashboardRepository
) {
    operator fun invoke(): Flow<DashboardMetrics> {
        return repository.getDashboardMetrics()
    }
}