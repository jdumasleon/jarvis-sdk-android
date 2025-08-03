package com.jarvis.demo.presentation.inspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.demo.data.repository.ApiCallResult
import com.jarvis.demo.data.repository.DemoApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectorViewModel @Inject constructor(
    private val demoApiRepository: DemoApiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InspectorUiState())
    val uiState: StateFlow<InspectorUiState> = _uiState.asStateFlow()
    
    private val _apiCalls = MutableStateFlow<List<ApiCallResult>>(emptyList())
    val apiCalls: StateFlow<List<ApiCallResult>> = _apiCalls.asStateFlow()
    
    init {
        performInitialApiCalls()
    }
    
    fun performInitialApiCalls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Perform 10-20 random API calls concurrently
                val numberOfCalls = (10..20).random()
                val apiCallJobs = (1..numberOfCalls).map {
                    async { 
                        val result = demoApiRepository.performRandomApiCall()
                        // Add to the list as each call completes
                        _apiCalls.value = _apiCalls.value + result
                        result
                    }
                }
                
                // Wait for all calls to complete
                apiCallJobs.awaitAll()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun addRandomApiCall() {
        viewModelScope.launch {
            try {
                val result = demoApiRepository.performRandomApiCall()
                _apiCalls.value = _apiCalls.value + result
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to perform API call"
                )
            }
        }
    }
    
    fun clearApiCalls() {
        _apiCalls.value = emptyList()
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class InspectorUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)