package com.jarvis.internal.feature.home.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.home.domain.entity.DashboardMetrics
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository

/**
 * Use case to refresh dashboard metrics
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class RefreshDashboardMetricsUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): DashboardMetrics {
        return repository.refreshMetrics()
    }
}