package com.jarvis.features.inspector.internal.domain.repository

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.entity.RuleApplicationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing network interception rules
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface NetworkRulesRepository {
    
    /**
     * Get all network rules
     */
    fun getAllRules(): Flow<List<NetworkRule>>
    
    /**
     * Get enabled network rules only
     */
    fun getEnabledRules(): Flow<List<NetworkRule>>
    
    /**
     * Get a specific rule by ID
     */
    suspend fun getRuleById(id: String): NetworkRule?
    
    /**
     * Save a new rule or update existing one
     */
    suspend fun saveRule(rule: NetworkRule): Result<NetworkRule>
    
    /**
     * Delete a rule
     */
    suspend fun deleteRule(id: String): Result<Unit>
    
    /**
     * Enable/disable a rule
     */
    suspend fun toggleRuleStatus(id: String, isEnabled: Boolean): Result<NetworkRule>
    
    /**
     * Get rule application history
     */
    fun getRuleApplicationHistory(): Flow<List<RuleApplicationResult>>
    
    /**
     * Record rule application
     */
    suspend fun recordRuleApplication(result: RuleApplicationResult)
    
    /**
     * Clear rule application history
     */
    suspend fun clearRuleApplicationHistory()
    
    /**
     * Import rules from JSON
     */
    suspend fun importRules(rulesJson: String): Result<List<NetworkRule>>
    
    /**
     * Export rules to JSON
     */
    suspend fun exportRules(): Result<String>
}