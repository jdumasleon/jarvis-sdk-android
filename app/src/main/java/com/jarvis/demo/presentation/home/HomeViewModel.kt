package com.jarvis.demo.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.demo.domain.usecase.home.RefreshDataUseCase
import com.jarvis.demo.domain.usecase.home.ManageJarvisModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val refreshDataUseCase: RefreshDataUseCase,
    private val manageJarvisModeUseCase: ManageJarvisModeUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    
    private val _uiState = MutableStateFlow<HomeUiState>(ResourceState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        Log.d("HomeViewModel", "ViewModel initialized with state: ${_uiState.value}")
        onEvent(HomeEvent.RefreshData)
    }
    
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.RefreshData -> refreshData()
            is HomeEvent.ToggleJarvisMode -> toggleJarvisMode()
            is HomeEvent.ClearError -> clearError()
        }
    }
    
    private fun refreshData() {
        Log.d("HomeViewModel", "Refresh action triggered from top app bar")
        
        viewModelScope.launch(ioDispatcher) {
            try {
                _uiState.update { ResourceState.Loading }

                // Make API calls through use case
                val result = withContext(ioDispatcher) {
                    refreshDataUseCase.execute()
                }

                val uiData = HomeUiData(
                    lastRefreshTime = System.currentTimeMillis(),
                    isJarvisActive = manageJarvisModeUseCase.isJarvisActive()
                )

                _uiState.update { ResourceState.Success(uiData) }
            } catch (exception: Exception) {
                Log.e("HomeViewModel", "Error during refresh", exception)
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to refresh data")
                }
            }
        }
    }
    
    private fun toggleJarvisMode() {
        val newActiveState = manageJarvisModeUseCase.toggleJarvisMode()

        // Update UI state regardless of current data state
        val currentData = _uiState.value.getDataOrNull() ?: HomeUiData()
        val updatedData = currentData.copy(isJarvisActive = newActiveState)
        _uiState.update { ResourceState.Success(updatedData) }
    }
    
    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            onEvent(HomeEvent.RefreshData)
        }
    }
}