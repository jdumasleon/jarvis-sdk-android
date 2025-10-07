package com.jarvis.demo.koin.presentation.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.demo.koin.data.repository.ApiCallResult
import com.jarvis.demo.koin.domain.usecase.inspector.PerformApiCallsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class InspectorViewModel (
    private val performApiCallsUseCase: PerformApiCallsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<InspectorUiState>(ResourceState.Idle)
    val uiState: StateFlow<InspectorUiState> = _uiState.asStateFlow()
    
    init {
        onEvent(InspectorEvent.PerformInitialApiCalls)
    }
    
    fun onEvent(event: InspectorEvent) {
        when (event) {
            is InspectorEvent.PerformInitialApiCalls -> performInitialApiCalls()
            is InspectorEvent.AddRandomApiCall -> addRandomApiCall()
            is InspectorEvent.ClearApiCalls -> clearApiCalls()
            is InspectorEvent.RefreshCalls -> refreshCalls()
            is InspectorEvent.CallSelected -> selectCall(event.call)
            is InspectorEvent.ShowClearConfirmation -> showClearConfirmation(event.show)
            is InspectorEvent.ClearError -> clearError()
        }
    }
    
    private fun performInitialApiCalls() {
        _uiState.update { ResourceState.Loading }
        viewModelScope.launch(ioDispatcher) {
            try {
                // Start with empty data but show we're performing calls
                val initialData = InspectorUiData(
                    apiCalls = emptyList(),
                    isPerformingCalls = true
                )
                _uiState.update { ResourceState.Success(initialData) }
                
                // Perform 10-20 random API calls concurrently
                val numberOfCalls = (10..20).random()
                val completedCalls = mutableListOf<ApiCallResult>()
                
                val apiCallJobs = (1..numberOfCalls).map {
                    async {
                        val result = performApiCallsUseCase.performRandomApiCall()
                        // Add to the list as each call completes
                        synchronized(completedCalls) {
                            completedCalls.add(result)
                            val currentData = _uiState.value.getDataOrNull() ?: initialData
                            val updatedData = currentData.copy(
                                apiCalls = completedCalls.sortedByDescending { it.startTime },
                                totalCallsPerformed = completedCalls.size,
                                successfulCalls = completedCalls.count { it.isSuccess },
                                failedCalls = completedCalls.count { !it.isSuccess }
                            )
                            _uiState.update { ResourceState.Success(updatedData) }
                        }
                        result
                    }
                }
                
                // Wait for all calls to complete
                apiCallJobs.awaitAll()
                
                // Final update to mark we're done performing calls
                val finalData = _uiState.value.getDataOrNull()?.copy(
                    isPerformingCalls = false
                ) ?: initialData.copy(isPerformingCalls = false)
                
                _uiState.update { ResourceState.Success(finalData) }
                
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to perform API calls")
                }
            }
        }
    }
    
    private fun addRandomApiCall() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val currentData = _uiState.value.getDataOrNull() ?: return@launch
                
                val result = performApiCallsUseCase.performRandomApiCall()
                val updatedCalls = listOf(result) + currentData.apiCalls
                val updatedData = currentData.copy(
                    apiCalls = updatedCalls,
                    totalCallsPerformed = updatedCalls.size,
                    successfulCalls = updatedCalls.count { it.isSuccess },
                    failedCalls = updatedCalls.count { !it.isSuccess }
                )
                
                _uiState.update { ResourceState.Success(updatedData) }
                
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to perform API call")
                }
            }
        }
    }
    
    private fun clearApiCalls() {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val clearedData = currentData.copy(
            apiCalls = emptyList(),
            totalCallsPerformed = 0,
            successfulCalls = 0,
            failedCalls = 0
        )
        _uiState.update { ResourceState.Success(clearedData) }
    }
    
    private fun refreshCalls() {
        viewModelScope.launch(ioDispatcher) {
            val currentData = _uiState.value.getDataOrNull() ?: InspectorUiData()
            
            // Set refresh state
            _uiState.update { ResourceState.Success(currentData.copy(isRefreshing = true)) }
            
            try {
                // Perform new API calls
                val apiCallJobs = (1..3).map { // Fewer calls for refresh
                    async {
                        val result = performApiCallsUseCase.performRandomApiCall()
                        
                        // Update state with each new call
                        _uiState.value.getDataOrNull()?.let { data ->
                            val updatedCalls = (listOf(result) + data.apiCalls).sortedByDescending { it.startTime }
                            val updatedData = data.copy(
                                apiCalls = updatedCalls,
                                totalCallsPerformed = data.totalCallsPerformed + 1,
                                successfulCalls = data.successfulCalls + if (result.isSuccess) 1 else 0,
                                failedCalls = data.failedCalls + if (!result.isSuccess) 1 else 0
                            )
                            _uiState.update { ResourceState.Success(updatedData) }
                        }
                        result
                    }
                }
                
                // Wait for all calls to complete
                apiCallJobs.awaitAll()
                
                // Turn off refresh indicator
                val finalData = _uiState.value.getDataOrNull()?.copy(isRefreshing = false)
                if (finalData != null) {
                    _uiState.update { ResourceState.Success(finalData) }
                }
                
            } catch (exception: Exception) {
                // Turn off refresh indicator even on error
                val errorData = _uiState.value.getDataOrNull()?.copy(isRefreshing = false)
                if (errorData != null) {
                    _uiState.update { ResourceState.Success(errorData) }
                }
            }
        }
    }
    
    private fun selectCall(call: ApiCallResult) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedCall = call)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun showClearConfirmation(show: Boolean) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(showClearConfirmation = show)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            onEvent(InspectorEvent.PerformInitialApiCalls)
        }
    }
}