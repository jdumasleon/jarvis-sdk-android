package com.jarvis.features.inspector.internal.data.repository

import androidx.annotation.RestrictTo

import android.content.Context
import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.entity.RuleApplicationResult
import com.jarvis.features.inspector.internal.domain.repository.NetworkRulesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NetworkRulesRepository using local file storage
 */
@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NetworkRulesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkRulesRepository {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val _rules = MutableStateFlow<List<NetworkRule>>(emptyList())
    private val _applicationHistory = MutableStateFlow<List<RuleApplicationResult>>(emptyList())
    
    private val rulesFile: File by lazy {
        File(context.filesDir, "jarvis_network_rules.json")
    }
    
    private val historyFile: File by lazy {
        File(context.filesDir, "jarvis_rule_application_history.json")
    }
    
    init {
        loadRulesFromDisk()
        loadHistoryFromDisk()
    }
    
    override fun getAllRules(): Flow<List<NetworkRule>> {
        return _rules.asStateFlow()
    }
    
    override fun getEnabledRules(): Flow<List<NetworkRule>> {
        return _rules.map { rules ->
            rules.filter { it.isEnabled }
        }
    }
    
    override suspend fun getRuleById(id: String): NetworkRule? {
        return _rules.value.find { it.id == id }
    }
    
    override suspend fun saveRule(rule: NetworkRule): Result<NetworkRule> {
        return try {
            val currentRules = _rules.value.toMutableList()
            val existingIndex = currentRules.indexOfFirst { it.id == rule.id }
            
            if (existingIndex >= 0) {
                currentRules[existingIndex] = rule
            } else {
                currentRules.add(rule)
            }
            
            _rules.value = currentRules
            saveRulesToDisk()
            
            Result.success(rule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteRule(id: String): Result<Unit> {
        return try {
            val currentRules = _rules.value.toMutableList()
            val removed = currentRules.removeAll { it.id == id }
            
            if (removed) {
                _rules.value = currentRules
                saveRulesToDisk()
                Result.success(Unit)
            } else {
                Result.failure(NoSuchElementException("Rule with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleRuleStatus(id: String, isEnabled: Boolean): Result<NetworkRule> {
        return try {
            val currentRules = _rules.value.toMutableList()
            val ruleIndex = currentRules.indexOfFirst { it.id == id }
            
            if (ruleIndex >= 0) {
                val updatedRule = currentRules[ruleIndex].copy(
                    isEnabled = isEnabled,
                    lastModified = System.currentTimeMillis()
                )
                currentRules[ruleIndex] = updatedRule
                _rules.value = currentRules
                saveRulesToDisk()
                
                Result.success(updatedRule)
            } else {
                Result.failure(NoSuchElementException("Rule with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getRuleApplicationHistory(): Flow<List<RuleApplicationResult>> {
        return _applicationHistory.asStateFlow()
    }
    
    override suspend fun recordRuleApplication(result: RuleApplicationResult) {
        try {
            val currentHistory = _applicationHistory.value.toMutableList()
            currentHistory.add(0, result) // Add to beginning for chronological order
            
            // Keep only last 100 applications to prevent memory issues
            if (currentHistory.size > 100) {
                currentHistory.subList(100, currentHistory.size).clear()
            }
            
            _applicationHistory.value = currentHistory
            saveHistoryToDisk()
        } catch (e: Exception) {
            // Log error but don't fail the network request
            android.util.Log.w("NetworkRulesRepository", "Failed to record rule application", e)
        }
    }
    
    override suspend fun clearRuleApplicationHistory() {
        _applicationHistory.value = emptyList()
        saveHistoryToDisk()
    }
    
    override suspend fun importRules(rulesJson: String): Result<List<NetworkRule>> {
        return try {
            val importedRules = json.decodeFromString<List<NetworkRule>>(rulesJson)
            val currentRules = _rules.value.toMutableList()
            
            // Add imported rules, replacing any with same ID
            importedRules.forEach { importedRule ->
                val existingIndex = currentRules.indexOfFirst { it.id == importedRule.id }
                if (existingIndex >= 0) {
                    currentRules[existingIndex] = importedRule
                } else {
                    currentRules.add(importedRule)
                }
            }
            
            _rules.value = currentRules
            saveRulesToDisk()
            
            Result.success(importedRules)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportRules(): Result<String> {
        return try {
            val rulesJson = json.encodeToString(_rules.value)
            Result.success(rulesJson)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun loadRulesFromDisk() {
        try {
            if (rulesFile.exists()) {
                val rulesJson = rulesFile.readText()
                if (rulesJson.isNotBlank()) {
                    val rules = json.decodeFromString<List<NetworkRule>>(rulesJson)
                    _rules.value = rules
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NetworkRulesRepository", "Failed to load rules from disk", e)
            // Initialize with empty list on error
            _rules.value = emptyList()
        }
    }
    
    private suspend fun saveRulesToDisk() = withContext(Dispatchers.IO) {
        try {
            val rulesJson = json.encodeToString(_rules.value)
            rulesFile.writeText(rulesJson)
        } catch (e: Exception) {
            android.util.Log.e("NetworkRulesRepository", "Failed to save rules to disk", e)
        }
    }
    
    private fun loadHistoryFromDisk() {
        try {
            if (historyFile.exists()) {
                val historyJson = historyFile.readText()
                if (historyJson.isNotBlank()) {
                    val history = json.decodeFromString<List<RuleApplicationResult>>(historyJson)
                    _applicationHistory.value = history
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NetworkRulesRepository", "Failed to load history from disk", e)
            _applicationHistory.value = emptyList()
        }
    }
    
    private suspend fun saveHistoryToDisk() = withContext(Dispatchers.IO) {
        try {
            val historyJson = json.encodeToString(_applicationHistory.value)
            historyFile.writeText(historyJson)
        } catch (e: Exception) {
            android.util.Log.e("NetworkRulesRepository", "Failed to save history to disk", e)
        }
    }
}