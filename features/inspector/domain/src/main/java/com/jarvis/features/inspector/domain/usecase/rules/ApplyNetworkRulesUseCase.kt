package com.jarvis.features.inspector.domain.usecase.rules

import com.jarvis.features.inspector.domain.entity.HttpMethod
import com.jarvis.features.inspector.domain.entity.NetworkRule
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.RuleApplicationResult
import com.jarvis.features.inspector.domain.entity.RuleMode
import com.jarvis.features.inspector.domain.repository.NetworkRulesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for applying network rules to intercept and modify requests/responses
 */
class ApplyNetworkRulesUseCase @Inject constructor(
    private val repository: NetworkRulesRepository
) {
    
    /**
     * Find matching rules for a network transaction
     */
    suspend fun findMatchingRules(
        transaction: NetworkTransaction,
        enabledRulesOnly: Boolean = true
    ): List<NetworkRule> {
        val allRulesFlow = if (enabledRulesOnly) {
            repository.getEnabledRules()
        } else {
            repository.getAllRules()
        }
        
        // Get the current list of rules from the flow
        val allRules = try {
            allRulesFlow.first()
        } catch (e: Exception) {
            emptyList<NetworkRule>()
        }
        
        return allRules.filter { rule ->
            rule.origin.matches(transaction)
        }.sortedBy { it.name } // Consistent ordering
    }
    
    /**
     * Apply request modifications from a rule
     */
    fun applyRequestModifications(
        originalRequest: com.jarvis.features.inspector.domain.entity.NetworkRequest,
        rule: NetworkRule
    ): com.jarvis.features.inspector.domain.entity.NetworkRequest {
        val modifications = rule.requestModifications ?: return originalRequest
        
        var modifiedRequest = originalRequest
        
        // Modify URL if specified
        modifications.modifyUrl?.let { newUrl ->
            modifiedRequest = modifiedRequest.copy(url = newUrl)
        }
        
        // Modify method if specified
        modifications.modifyMethod?.let { newMethod ->
            modifiedRequest = modifiedRequest.copy(method = HttpMethod.fromString(newMethod))
        }
        
        // Modify headers
        val newHeaders = modifiedRequest.headers.toMutableMap()
        
        // Add new headers
        newHeaders.putAll(modifications.addHeaders)
        
        // Modify existing headers
        modifications.modifyHeaders.forEach { (key, value) ->
            newHeaders[key] = value
        }
        
        // Remove headers
        modifications.removeHeaders.forEach { key ->
            newHeaders.remove(key)
        }
        
        modifiedRequest = modifiedRequest.copy(headers = newHeaders)
        
        // Modify body if specified
        modifications.modifyBody?.let { newBody ->
            modifiedRequest = modifiedRequest.copy(body = newBody)
        }
        
        return modifiedRequest
    }
    
    /**
     * Apply response modifications from a rule
     */
    fun applyResponseModifications(
        originalResponse: com.jarvis.features.inspector.domain.entity.NetworkResponse,
        rule: NetworkRule
    ): com.jarvis.features.inspector.domain.entity.NetworkResponse {
        val modifications = rule.responseModifications ?: return originalResponse
        
        var modifiedResponse = originalResponse
        
        // Modify status code if specified
        modifications.statusCode?.let { newStatusCode ->
            modifiedResponse = modifiedResponse.copy(statusCode = newStatusCode)
        }
        
        // Modify status message if specified
        modifications.statusMessage?.let { newStatusMessage ->
            modifiedResponse = modifiedResponse.copy(statusMessage = newStatusMessage)
        }
        
        // Modify headers
        val newHeaders = modifiedResponse.headers.toMutableMap()
        
        // Add new headers
        newHeaders.putAll(modifications.addHeaders)
        
        // Modify existing headers
        modifications.modifyHeaders.forEach { (key, value) ->
            newHeaders[key] = value
        }
        
        // Remove headers
        modifications.removeHeaders.forEach { key ->
            newHeaders.remove(key)
        }
        
        modifiedResponse = modifiedResponse.copy(headers = newHeaders)
        
        // Modify body if specified
        modifications.modifyBody?.let { newBody ->
            modifiedResponse = modifiedResponse.copy(body = newBody)
        }
        
        return modifiedResponse
    }
    
    /**
     * Create a mock response based on rule specifications
     */
    fun createMockResponse(
        request: com.jarvis.features.inspector.domain.entity.NetworkRequest,
        rule: NetworkRule
    ): com.jarvis.features.inspector.domain.entity.NetworkResponse {
        val modifications = rule.responseModifications
        
        return com.jarvis.features.inspector.domain.entity.NetworkResponse(
            statusCode = modifications?.statusCode ?: 200,
            statusMessage = modifications?.statusMessage ?: "OK",
            headers = modifications?.addHeaders ?: emptyMap(),
            body = modifications?.modifyBody ?: """{"mock": true, "rule": "${rule.name}"}""",
            bodySize = modifications?.modifyBody?.length?.toLong() ?: 0L,
            contentType = modifications?.addHeaders?.get("Content-Type") ?: "application/json",
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Record rule application for history/debugging
     */
    suspend fun recordRuleApplication(
        rule: NetworkRule,
        transaction: NetworkTransaction,
        modificationsApplied: List<String>
    ) {
        val result = RuleApplicationResult(
            ruleId = rule.id,
            ruleName = rule.name,
            mode = rule.mode,
            applied = true,
            modificationsApplied = modificationsApplied
        )
        
        repository.recordRuleApplication(result)
    }
}

