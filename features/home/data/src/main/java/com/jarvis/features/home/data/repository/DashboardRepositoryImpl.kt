package com.jarvis.features.home.data.repository

import com.jarvis.features.home.data.mapper.DashboardMetricsMapper
import com.jarvis.features.home.data.mapper.EnhancedDashboardMetricsMapper
import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.features.home.domain.entity.SessionFilter
import com.jarvis.features.home.domain.repository.DashboardRepository
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * Implementation of DashboardRepository
 * Aggregates data from Inspector and Preferences repositories with enhanced analytics
 */
class DashboardRepositoryImpl @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferencesRepository: PreferencesRepository,
    private val mapper: DashboardMetricsMapper,
    private val enhancedMapper: EnhancedDashboardMetricsMapper,
    @com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher 
    private val ioDispatcher: CoroutineDispatcher
) : DashboardRepository {

    override fun getDashboardMetrics(): Flow<DashboardMetrics> {
        return combine(
            networkRepository.getAllTransactions(),
            preferencesRepository.getAllPreferences()
        ) { networkTransactions, preferences ->
            mapper.mapToDashboardMetrics(networkTransactions, preferences)
        }.flowOn(ioDispatcher) // Move computation off main thread
    }

    override fun getEnhancedDashboardMetrics(sessionFilter: SessionFilter): Flow<EnhancedDashboardMetrics> {
        return combine(
            // ✅ LAZY LOADING: Use paginated data for better performance (limit to most recent 200 transactions)
            networkRepository.getTransactionsPaged(limit = 200, offset = 0),
            preferencesRepository.getAllPreferences()
        ) { networkTransactions, preferences ->
            // ✅ CRITICAL FIX: Move heavy computation to background thread
            enhancedMapper.mapToEnhancedDashboardMetrics(
                networkTransactions = networkTransactions,
                preferences = preferences,
                sessionFilter = sessionFilter
            )
        }.flowOn(ioDispatcher) // Move ALL expensive analytics off main thread
    }

    override suspend fun refreshMetrics(): DashboardMetrics {
        // Get the latest values from flows for refresh
        val networkTransactions = networkRepository.getAllTransactions().first()
        val preferences = preferencesRepository.getAllPreferences().first()
        
        return mapper.mapToDashboardMetrics(networkTransactions, preferences)
    }
}