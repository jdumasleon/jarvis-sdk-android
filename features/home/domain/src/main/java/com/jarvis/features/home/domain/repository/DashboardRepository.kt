package com.jarvis.features.home.domain.repository

import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.features.home.domain.entity.DashboardLayout
import com.jarvis.features.home.domain.entity.SessionFilter
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
     * Get enhanced dashboard metrics with session filtering and advanced analytics
     */
    fun getEnhancedDashboardMetrics(sessionFilter: SessionFilter): Flow<EnhancedDashboardMetrics>
    
    /**
     * Get current dashboard layout configuration
     */
    suspend fun getDashboardLayout(): DashboardLayout
    
    /**
     * Update dashboard layout configuration
     */
    suspend fun updateDashboardLayout(layout: DashboardLayout)
    
    /**
     * Refresh all metrics data
     */
    suspend fun refreshMetrics(): DashboardMetrics
}