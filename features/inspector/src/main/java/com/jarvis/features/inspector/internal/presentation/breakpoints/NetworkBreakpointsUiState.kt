@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.presentation.breakpoints

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.entity.RuleApplicationResult

/**
 * UI state for Network Rules screen
 */
typealias NetworkBreackpointsUiState = ResourceState<NetworkBreakpointsData>

/**
 * Data for Network Rules screen
 */
data class NetworkBreakpointsData(
    val rules: List<NetworkRule> = emptyList(),
    val enabledRulesCount: Int = 0,
    val applicationHistory: List<RuleApplicationResult> = emptyList(),
    val selectedRule: NetworkRule? = null,
    val showRuleEditor: Boolean = false,
    val showImportExport: Boolean = false
) {
    companion object {
        fun empty() = NetworkBreakpointsData()
        
        fun mockBreakpointsData() = NetworkBreakpointsData(
            rules = listOf(
                NetworkRule(
                    id = "1",
                    name = "API Response Modifier",
                    isEnabled = true,
                    origin = com.jarvis.features.inspector.internal.domain.entity.RuleOrigin(
                        protocols = listOf("https"),
                        hostUrl = "api.example.com",
                        path = "/users/*",
                        method = "GET"
                    ),
                    mode = com.jarvis.features.inspector.internal.domain.entity.RuleMode.INSPECT,
                    responseModifications = com.jarvis.features.inspector.internal.domain.entity.ResponseModifications(
                        addHeaders = mapOf("X-Mock" to "true"),
                        modifyBody = """{"mock": true, "users": []}"""
                    )
                ),
                NetworkRule(
                    id = "2", 
                    name = "Login Mock",
                    isEnabled = false,
                    origin = com.jarvis.features.inspector.internal.domain.entity.RuleOrigin(
                        protocols = listOf("https"),
                        hostUrl = "auth.example.com",
                        path = "/login",
                        method = "POST"
                    ),
                    mode = com.jarvis.features.inspector.internal.domain.entity.RuleMode.MOCK,
                    responseModifications = com.jarvis.features.inspector.internal.domain.entity.ResponseModifications(
                        statusCode = 200,
                        modifyBody = """{"token": "mock_token", "user": {"id": 1, "name": "Mock User"}}"""
                    )
                )
            ),
            enabledRulesCount = 1,
            applicationHistory = listOf(
                RuleApplicationResult(
                    ruleId = "1",
                    ruleName = "API Response Modifier", 
                    mode = com.jarvis.features.inspector.internal.domain.entity.RuleMode.INSPECT,
                    applied = true,
                    modificationsApplied = listOf("Added header: X-Mock", "Modified response body"),
                    timestamp = System.currentTimeMillis() - 60000
                )
            )
        )
    }
}

/**
 * Events for Network Rules screen
 */
sealed class NetworkRulesEvent {
    data object LoadRules : NetworkRulesEvent()
    data object RefreshRules : NetworkRulesEvent()
    data object ClearError : NetworkRulesEvent()
    
    // Rule management
    data class SelectRule(val rule: NetworkRule?) : NetworkRulesEvent()
    data object CreateNewRule : NetworkRulesEvent()
    data class EditRule(val rule: NetworkRule) : NetworkRulesEvent()
    data class DeleteRule(val ruleId: String) : NetworkRulesEvent()
    data class ToggleRule(val ruleId: String, val enabled: Boolean) : NetworkRulesEvent()
    data class SaveRule(val rule: NetworkRule) : NetworkRulesEvent()
    
    // UI state
    data class ShowRuleEditor(val show: Boolean) : NetworkRulesEvent()
    data class ShowImportExport(val show: Boolean) : NetworkRulesEvent()
    
    // Import/Export
    data class ImportRules(val rulesJson: String) : NetworkRulesEvent()
    data object ExportRules : NetworkRulesEvent()
    
    // History
    data object ClearHistory : NetworkRulesEvent()
}