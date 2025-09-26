package com.jarvis.features.inspector.internal.domain.usecase.rules

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.repository.NetworkRulesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving network rules
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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