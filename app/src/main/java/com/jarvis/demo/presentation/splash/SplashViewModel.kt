package com.jarvis.demo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.common.di.CoroutineDispatcherModule
import com.jarvis.core.presentation.state.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @param:CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(ResourceState.Idle)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        onEvent(SplashEvent.StartSplash)
    }
    
    fun onEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.StartSplash -> startSplash()
            is SplashEvent.CompleteSplash -> completeSplash()
            is SplashEvent.ClearError -> clearError()
        }
    }
    
    private fun startSplash() {
        viewModelScope.launch(ioDispatcher) {
            try {
                // Start with initial data
                val initialData = SplashUiData(
                    showSplash = true,
                    loadingProgress = 0f,
                    initializationMessage = "Starting up..."
                )
                _uiState.update { ResourceState.Success(initialData) }
                
                // Simulate initialization phases
                val phases = listOf(
                    "Initializing Jarvis SDK..." to 0.2f,
                    "Setting up network monitoring..." to 0.5f,
                    "Loading configuration..." to 0.8f,
                    "Ready!" to 1f
                )
                
                phases.forEach { (message, progress) ->
                    delay(500) // Each phase takes 500ms
                    val currentData = _uiState.value.getDataOrNull() ?: initialData
                    val updatedData = currentData.copy(
                        loadingProgress = progress,
                        initializationMessage = message
                    )
                    _uiState.update { ResourceState.Success(updatedData) }
                }
                
                // Complete splash after total 2 seconds
                delay(100)
                onEvent(SplashEvent.CompleteSplash)
                
            } catch (exception: Exception) {
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to initialize application")
                }
            }
        }
    }
    
    private fun completeSplash() {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val completedData = currentData.copy(
            showSplash = false,
            loadingProgress = 1f,
            initializationMessage = "Initialization complete!"
        )
        _uiState.update { ResourceState.Success(completedData) }
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            onEvent(SplashEvent.StartSplash)
        }
    }
}