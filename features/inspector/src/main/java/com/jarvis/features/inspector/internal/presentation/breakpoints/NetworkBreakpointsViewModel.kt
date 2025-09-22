package com.jarvis.features.inspector.internal.presentation.breakpoints

import androidx.annotation.RestrictTo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.features.inspector.internal.domain.entity.NetworkRule
import com.jarvis.features.inspector.internal.domain.entity.RuleMode
import com.jarvis.features.inspector.internal.domain.entity.RuleOrigin
import com.jarvis.features.inspector.internal.domain.usecase.rules.GetNetworkRulesUseCase
import com.jarvis.features.inspector.internal.domain.usecase.rules.ManageNetworkRulesUseCase
import com.jarvis.features.inspector.internal.domain.repository.NetworkRulesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing network interception rules
 */
@HiltViewModel
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NetworkBreakpointsViewModel @Inject constructor(
    private val getRulesUseCase: GetNetworkRulesUseCase,
    private val manageRulesUseCase: ManageNetworkRulesUseCase,
    private val repository: NetworkRulesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<NetworkBreackpointsUiState>(ResourceState.Loading)
    val uiState: StateFlow<NetworkBreackpointsUiState> = _uiState.asStateFlow()
    
    init {
        loadRules()
    }
    
    fun onEvent(event: NetworkRulesEvent) {
        when (event) {
            NetworkRulesEvent.LoadRules -> loadRules()
            NetworkRulesEvent.RefreshRules -> refreshRules()
            NetworkRulesEvent.ClearError -> clearError()
            
            is NetworkRulesEvent.SelectRule -> selectRule(event.rule)
            NetworkRulesEvent.CreateNewRule -> createNewRule()
            is NetworkRulesEvent.EditRule -> editRule(event.rule)
            is NetworkRulesEvent.DeleteRule -> deleteRule(event.ruleId)
            is NetworkRulesEvent.ToggleRule -> toggleRule(event.ruleId, event.enabled)
            is NetworkRulesEvent.SaveRule -> saveRule(event.rule)
            
            is NetworkRulesEvent.ShowRuleEditor -> showRuleEditor(event.show)
            is NetworkRulesEvent.ShowImportExport -> showImportExport(event.show)
            
            is NetworkRulesEvent.ImportRules -> importRules(event.rulesJson)
            NetworkRulesEvent.ExportRules -> exportRules()
            
            NetworkRulesEvent.ClearHistory -> clearHistory()
        }
    }
    
    private fun loadRules() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = ResourceState.Loading
            
            try {
                combine(
                    getRulesUseCase(),
                    repository.getRuleApplicationHistory()
                ) { rules, history ->
                    val enabledCount = rules.count { it.isEnabled }
                    val currentData = _uiState.value.getDataOrNull() ?: NetworkBreakpointsData.empty()
                    
                    NetworkBreakpointsData(
                        rules = rules,
                        enabledRulesCount = enabledCount,
                        applicationHistory = history.take(50), // Limit to last 50 applications
                        selectedRule = currentData.selectedRule,
                        showRuleEditor = currentData.showRuleEditor,
                        showImportExport = currentData.showImportExport
                    )
                }.catch { throwable ->
                    _uiState.value = ResourceState.Error(throwable, "Failed to load network rules")
                }.collect { data ->
                    _uiState.value = ResourceState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to load network rules")
            }
        }
    }
    
    private fun refreshRules() {
        loadRules()
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.value = ResourceState.Success(currentData)
        } else {
            loadRules()
        }
    }
    
    private fun selectRule(rule: NetworkRule?) {
        updateData { it.copy(selectedRule = rule) }
    }
    
    private fun createNewRule() {
        val newRule = NetworkRule(
            name = "New Rule",
            origin = RuleOrigin(),
            mode = RuleMode.INSPECT
        )
        updateData { 
            it.copy(
                selectedRule = newRule,
                showRuleEditor = true
            )
        }
    }
    
    private fun editRule(rule: NetworkRule) {
        updateData { 
            it.copy(
                selectedRule = rule,
                showRuleEditor = true
            )
        }
    }
    
    private fun deleteRule(ruleId: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = manageRulesUseCase.deleteRule(ruleId)
                if (result.isFailure) {
                    val error = result.exceptionOrNull() ?: Exception("Failed to delete rule")
                    _uiState.value = ResourceState.Error(error, "Failed to delete rule")
                }
                // Rules will be updated automatically through the flow
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to delete rule")
            }
        }
    }
    
    private fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = manageRulesUseCase.toggleRuleStatus(ruleId, enabled)
                if (result.isFailure) {
                    val error = result.exceptionOrNull() ?: Exception("Failed to toggle rule")
                    _uiState.value = ResourceState.Error(error, "Failed to toggle rule")
                }
                // Rules will be updated automatically through the flow
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to toggle rule")
            }
        }
    }
    
    private fun saveRule(rule: NetworkRule) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = manageRulesUseCase.saveRule(rule)
                if (result.isSuccess) {
                    updateData { 
                        it.copy(
                            selectedRule = null,
                            showRuleEditor = false
                        )
                    }
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Failed to save rule")
                    _uiState.value = ResourceState.Error(error, "Failed to save rule")
                }
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to save rule")
            }
        }
    }
    
    private fun showRuleEditor(show: Boolean) {
        updateData { 
            it.copy(
                showRuleEditor = show,
                selectedRule = if (show) it.selectedRule else null
            )
        }
    }
    
    private fun showImportExport(show: Boolean) {
        updateData { it.copy(showImportExport = show) }
    }
    
    private fun importRules(rulesJson: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = manageRulesUseCase.importRules(rulesJson)
                if (result.isFailure) {
                    val error = result.exceptionOrNull() ?: Exception("Failed to import rules")
                    _uiState.value = ResourceState.Error(error, "Failed to import rules")
                }
                updateData { it.copy(showImportExport = false) }
                // Rules will be updated automatically through the flow
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to import rules")
            }
        }
    }
    
    private fun exportRules() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val result = manageRulesUseCase.exportRules()
                if (result.isSuccess) {
                    // In a real app, you might want to save to file or share
                    val rulesJson = result.getOrNull()
                    Log.d("NetworkRulesViewModel", "Exported rules: $rulesJson")
                } else {
                    val error = result.exceptionOrNull() ?: Exception("Failed to export rules")
                    _uiState.value = ResourceState.Error(error, "Failed to export rules")
                }
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to export rules")
            }
        }
    }
    
    private fun clearHistory() {
        viewModelScope.launch(ioDispatcher) {
            try {
                repository.clearRuleApplicationHistory()
                // History will be updated automatically through the flow
            } catch (e: Exception) {
                _uiState.value = ResourceState.Error(e, "Failed to clear history")
            }
        }
    }
    
    private fun updateData(transform: (NetworkBreakpointsData) -> NetworkBreakpointsData) {
        val currentData = _uiState.value.getDataOrNull() ?: NetworkBreakpointsData.empty()
        _uiState.value = ResourceState.Success(transform(currentData))
    }
}