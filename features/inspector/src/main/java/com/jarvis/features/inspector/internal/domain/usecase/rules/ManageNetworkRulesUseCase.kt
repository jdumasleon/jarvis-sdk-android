package com.jarvis.features.inspector.internal.domain.usecase.rules

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.repository.NetworkRulesRepository
import javax.inject.Inject

/**
 * Use case for managing network rules (create, update, delete)
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ManageNetworkRulesUseCase @Inject constructor(
    private val repository: NetworkRulesRepository
) {
    
    /**
     * Save a new rule or update existing one
     */
    suspend fun saveRule(rule: NetworkRule): Result<NetworkRule> {
        val updatedRule = rule.copy(
            lastModified = System.currentTimeMillis()
        )
        return repository.saveRule(updatedRule)
    }
    
    /**
     * Delete a rule
     */
    suspend fun deleteRule(id: String): Result<Unit> {
        return repository.deleteRule(id)
    }
    
    /**
     * Toggle rule enabled/disabled status
     */
    suspend fun toggleRuleStatus(id: String, isEnabled: Boolean): Result<NetworkRule> {
        return repository.toggleRuleStatus(id, isEnabled)
    }
    
    /**
     * Import rules from JSON
     */
    suspend fun importRules(rulesJson: String): Result<List<NetworkRule>> {
        return repository.importRules(rulesJson)
    }
    
    /**
     * Export rules to JSON
     */
    suspend fun exportRules(): Result<String> {
        return repository.exportRules()
    }
}