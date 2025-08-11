package com.jarvis.features.home.data.repository

import com.jarvis.features.home.data.mapper.DashboardMetricsMapper
import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.repository.DashboardRepository
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of DashboardRepository
 * Aggregates data from Inspector and Preferences repositories
 */
class DashboardRepositoryImpl @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val preferencesRepository: PreferencesRepository,
    private val mapper: DashboardMetricsMapper
) : DashboardRepository {

    override fun getDashboardMetrics(): Flow<DashboardMetrics> {
        return combine(
            networkRepository.getAllTransactions(),
            preferencesRepository.getAllPreferences()
        ) { networkTransactions, preferences ->
            mapper.mapToDashboardMetrics(networkTransactions, preferences)
        }
    }

    override suspend fun refreshMetrics(): DashboardMetrics {
        // Get the latest values from flows for refresh
        val networkTransactions = networkRepository.getAllTransactions().first()
        val preferences = preferencesRepository.getAllPreferences().first()
        
        return mapper.mapToDashboardMetrics(networkTransactions, preferences)
    }
}