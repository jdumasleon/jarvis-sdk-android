package com.jarvis.internal.feature.home.domain.repository

import androidx.annotation.RestrictTo

import com.jarvis.internal.feature.home.domain.entity.DashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for dashboard metrics data
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface DashboardRepository {
    /**
     * Get real-time dashboard metrics
     */
    fun getDashboardMetrics(): Flow<DashboardMetrics>
    
    /**
     * Get enhanced dashboard metrics with session filtering and advanced analytics
     */
    fun getEnhancedDashboardMetrics(sessionFilter: SessionFilter): Flow<EnhancedDashboardMetrics>
    
    /**
     * Refresh all metrics data
     */
    suspend fun refreshMetrics(): DashboardMetrics
}