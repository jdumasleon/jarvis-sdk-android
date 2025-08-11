package com.jarvis.features.home.domain.repository

import com.jarvis.features.home.domain.entity.DashboardMetrics
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for dashboard metrics data
 */
interface DashboardRepository {
    /**
     * Get real-time dashboard metrics
     */
    fun getDashboardMetrics(): Flow<DashboardMetrics>
    
    /**
     * Refresh all metrics data
     */
    suspend fun refreshMetrics(): DashboardMetrics
}