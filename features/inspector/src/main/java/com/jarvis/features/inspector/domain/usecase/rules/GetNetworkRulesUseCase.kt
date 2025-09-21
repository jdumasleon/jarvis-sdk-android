package com.jarvis.features.inspector.domain.usecase.rules

import com.jarvis.features.inspector.domain.entity.NetworkRule
import com.jarvis.features.inspector.domain.repository.NetworkRulesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving network rules
 */
class GetNetworkRulesUseCase @Inject constructor(
    private val repository: NetworkRulesRepository
) {
    
    /**
     * Get all rules
     */
    operator fun invoke(): Flow<List<NetworkRule>> {
        return repository.getAllRules()
    }
    
    /**
     * Get only enabled rules
     */
    fun getEnabledRules(): Flow<List<NetworkRule>> {
        return repository.getEnabledRules()
    }
    
    /**
     * Get specific rule by ID
     */
    suspend fun getRuleById(id: String): NetworkRule? {
        return repository.getRuleById(id)
    }
}