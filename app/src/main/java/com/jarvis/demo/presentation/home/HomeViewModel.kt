package com.jarvis.demo.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.common.di.CoroutineDispatcherModule
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.data.api.FakeStoreApiService
import com.jarvis.demo.data.api.RestfulApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fakeStoreApiService: FakeStoreApiService,
    private val restfulApiService: RestfulApiService,
    private val jarvisSDK: com.jarvis.api.core.JarvisSDK,
    @param:CoroutineDispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
        
        viewModelScope.launch {
            try {
                _uiState.update { ResourceState.Loading }
                
                // Make API calls to generate network traffic for inspection on IO dispatcher
                val result = withContext(ioDispatcher) {
                    val productsCall = async { fakeStoreApiService.getAllProducts() }
                    val categoriesCall = async { fakeStoreApiService.getAllCategories() }
                    val objectsCall = async { restfulApiService.getAllObjects() }
                    
                    // Execute API calls and log results
                    val productsResponse = productsCall.await()
                    val categoriesResponse = categoriesCall.await()
                    val objectsResponse = objectsCall.await()
                    
                    Log.d("HomeViewModel", "Products response: ${productsResponse.code()}")
                    Log.d("HomeViewModel", "Categories response: ${categoriesResponse.code()}")
                    Log.d("HomeViewModel", "Objects response: ${objectsResponse.code()}")
                    
                    Triple(productsResponse, categoriesResponse, objectsResponse)
                }
                
                val uiData = HomeUiData(
                    lastRefreshTime = System.currentTimeMillis(),
                    isJarvisActive = jarvisSDK.isActive()
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
        val newActiveState = jarvisSDK.toggle()
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(isJarvisActive = newActiveState)
        _uiState.update { ResourceState.Success(updatedData) }
        
        Log.d("HomeViewModel", "Jarvis mode toggled: $newActiveState")
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