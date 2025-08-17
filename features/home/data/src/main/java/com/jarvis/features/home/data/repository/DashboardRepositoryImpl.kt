package com.jarvis.features.home.data.repository

import com.jarvis.features.home.data.mapper.DashboardMetricsMapper
import com.jarvis.features.home.data.mapper.EnhancedDashboardMetricsMapper
import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.features.home.domain.entity.SessionFilter
import com.jarvis.features.home.domain.repository.DashboardRepository
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of DashboardRepository
 * Aggregates data from Inspector and Preferences repositories with enhanced analytics
 */
class DashboardRepositoryImpl @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferencesRepository: PreferencesRepository,
    private val mapper: DashboardMetricsMapper,
    private val enhancedMapper: EnhancedDashboardMetricsMapper
) : DashboardRepository {

    override fun getDashboardMetrics(): Flow<DashboardMetrics> {
        return combine(
            networkRepository.getAllTransactions(),
            preferencesRepository.getAllPreferences()
        ) { networkTransactions, preferences ->
            mapper.mapToDashboardMetrics(networkTransactions, preferences)
        }
    }

    override fun getEnhancedDashboardMetrics(sessionFilter: SessionFilter): Flow<EnhancedDashboardMetrics> {
        return combine(
            networkRepository.getAllTransactions(),
            preferencesRepository.getAllPreferences()
        ) { networkTransactions, preferences ->
            enhancedMapper.mapToEnhancedDashboardMetrics(
                networkTransactions = networkTransactions,
                preferences = preferences,
                sessionFilter = sessionFilter
            )
        }
    }

    override suspend fun refreshMetrics(): DashboardMetrics {
        // Get the latest values from flows for refresh
        val networkTransactions = networkRepository.getAllTransactions().first()
        val preferences = preferencesRepository.getAllPreferences().first()
        
        return mapper.mapToDashboardMetrics(networkTransactions, preferences)
    }
}