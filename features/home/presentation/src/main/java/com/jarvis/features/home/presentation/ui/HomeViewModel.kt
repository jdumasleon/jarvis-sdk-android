package com.jarvis.features.home.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.domain.performance.GetPerformanceMetricsUseCase
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.home.domain.usecase.GetDashboardMetricsUseCase
import com.jarvis.features.home.domain.usecase.RefreshDashboardMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen following ResourceState pattern
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDashboardMetricsUseCase: GetDashboardMetricsUseCase,
    private val refreshDashboardMetricsUseCase: RefreshDashboardMetricsUseCase,
    private val getPerformanceMetricsUseCase: GetPerformanceMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(ResourceState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.RefreshDashboard -> refreshDashboard()
            is HomeEvent.TabSelected -> updateSelectedTab(event.tab)
            is HomeEvent.NavigateToInspector -> handleNavigateToInspector()
            is HomeEvent.NavigateToPreferences -> handleNavigateToPreferences()
            is HomeEvent.ClearError -> clearError()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { ResourceState.Loading }
            
            combine(
                getDashboardMetricsUseCase(),
                getPerformanceMetricsUseCase()
            ) { dashboardMetrics, performanceSnapshot ->
                HomeUiData(
                    dashboardMetrics = dashboardMetrics,
                    performanceSnapshot = performanceSnapshot,
                    selectedTab = _uiState.value.getDataOrNull()?.selectedTab ?: HomeTab.DASHBOARD
                )
            }
            .onStart { }
            .catch { exception ->
                _uiState.update { 
                    ResourceState.Error(exception, "Failed to load dashboard and performance metrics")
                }
            }
            .collect { homeUiData ->
                _uiState.update { ResourceState.Success(homeUiData) }
            }
        }
    }

    private fun refreshDashboard() {
        viewModelScope.launch {
            val currentData = _uiState.value.getDataOrNull()
            if (currentData != null) {
                _uiState.update { 
                    ResourceState.Success(currentData.copy(isRefreshing = true))
                }
            }
            
            combine(
                getDashboardMetricsUseCase(),
                getPerformanceMetricsUseCase()
            ) { dashboardMetrics, performanceSnapshot ->
                HomeUiData(
                    dashboardMetrics = dashboardMetrics,
                    performanceSnapshot = performanceSnapshot,
                    selectedTab = currentData?.selectedTab ?: HomeTab.DASHBOARD,
                    isRefreshing = false
                )
            }
            .catch { exception ->
                val errorData = currentData?.copy(isRefreshing = false)
                if (errorData != null) {
                    _uiState.update { ResourceState.Success(errorData) }
                } else {
                    _uiState.update { 
                        ResourceState.Error(exception, "Failed to refresh dashboard and performance metrics")
                    }
                }
            }
            .collect { homeUiData ->
                _uiState.update { ResourceState.Success(homeUiData) }
            }
        }
    }

    private fun updateSelectedTab(tab: HomeTab) {
        val currentData = _uiState.value.getDataOrNull() ?: return
        val updatedData = currentData.copy(selectedTab = tab)
        _uiState.update { ResourceState.Success(updatedData) }
    }

    private fun handleNavigateToInspector() {
        // This will be handled by the composable through event callbacks
        // The navigation logic is passed from the parent composable
    }

    private fun handleNavigateToPreferences() {
        // This will be handled by the composable through event callbacks
        // The navigation logic is passed from the parent composable
    }

    private fun clearError() {
        val currentData = _uiState.value.getDataOrNull()
        if (currentData != null) {
            _uiState.update { ResourceState.Success(currentData) }
        } else {
            loadDashboard()
        }
    }
}