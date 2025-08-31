package com.jarvis.demo.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.api.JarvisSDK
import com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher
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
    private val jarvisSDK: JarvisSDK,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    // ✅ PERFORMANCE: Cache API results and throttle requests
    private var lastApiCallTime = 0L
    private var cachedApiResults: Triple<retrofit2.Response<*>?, retrofit2.Response<*>?, retrofit2.Response<*>?>? = null
    
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

                // ✅ PERFORMANCE: Make API calls with timeout and throttling to prevent excessive requests
                val result = withContext(ioDispatcher) {
                    try {
                        // Only make API calls every 10 seconds to reduce load
                        val currentTime = System.currentTimeMillis()
                        val timeSinceLastCall = currentTime - lastApiCallTime

                        if (timeSinceLastCall < 10000) { // 10 seconds throttle
                            return@withContext cachedApiResults ?: Triple(null, null, null)
                        }

                        lastApiCallTime = currentTime

                        val productsCall = async {
                            kotlinx.coroutines.withTimeoutOrNull(3000) { // Reduced to 3s
                                fakeStoreApiService.getAllProducts()
                            }
                        }
                        val categoriesCall = async {
                            kotlinx.coroutines.withTimeoutOrNull(3000) {
                                fakeStoreApiService.getAllCategories()
                            }
                        }
                        val objectsCall = async {
                            kotlinx.coroutines.withTimeoutOrNull(3000) {
                                restfulApiService.getAllObjects()
                            }
                        }

                        // Execute API calls with timeout protection and log results
                        val productsResponse = productsCall.await()
                        val categoriesResponse = categoriesCall.await()
                        val objectsResponse = objectsCall.await()

                        Log.d("HomeViewModel", "Products response: ${productsResponse?.code() ?: "timeout"}")
                        Log.d("HomeViewModel", "Categories response: ${categoriesResponse?.code() ?: "timeout"}")
                        Log.d("HomeViewModel", "Objects response: ${objectsResponse?.code() ?: "timeout"}")

                        val result = Triple(productsResponse, categoriesResponse, objectsResponse)
                        cachedApiResults = result // Cache the results
                        result
                    } catch (e: Exception) {
                        Log.w("HomeViewModel", "Some API calls failed", e)
                        cachedApiResults ?: Triple(null, null, null) // Return cached or null values
                    }
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
        
        // Update UI state regardless of current data state
        val currentData = _uiState.value.getDataOrNull() ?: HomeUiData()
        val updatedData = currentData.copy(isJarvisActive = newActiveState)
        _uiState.update { ResourceState.Success(updatedData) }
        
        Log.d("HomeViewModel", "Jarvis mode toggled: $newActiveState")
        
        // Log SDK state for debugging
        Log.d("HomeViewModel", "SDK isActive(): ${jarvisSDK.isActive()}")
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