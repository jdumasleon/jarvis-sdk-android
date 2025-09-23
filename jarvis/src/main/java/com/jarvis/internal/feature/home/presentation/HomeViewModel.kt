package com.jarvis.internal.feature.home.presentation

import androidx.annotation.RestrictTo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.domain.performance.GetPerformanceMetricsUseCase
import com.jarvis.core.internal.domain.preferences.usecase.ManageHeaderContentStateUseCase
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.internal.feature.home.domain.entity.DashboardCardType
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import javax.inject.Inject

/**
 * ViewModel for Home screen following the standard feature architecture pattern
 */
@OptIn(FlowPreview::class)
@HiltViewModel
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class HomeViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val getPerformanceMetricsUseCase: GetPerformanceMetricsUseCase,
    private val manageHeaderContentStateUseCase: ManageHeaderContentStateUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(ResourceState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        onEvent(HomeEvent.RefreshDashboard)
        startContinuousPerformanceMonitoring()
    }
    
    private fun startContinuousPerformanceMonitoring() {
        viewModelScope.launch(ioDispatcher) {
            getPerformanceMetricsUseCase()
                .catch { /* Handle errors gracefully */ }
                .collect { performanceSnapshot ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ResourceState.Success -> {
                                ResourceState.Success(
                                    currentState.data.copy(
                                        performanceSnapshot = performanceSnapshot,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                )
                            }
                            else -> currentState
                        }
                    }
                }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.RefreshDashboard -> loadDashboard()
            is HomeEvent.ChangeSessionFilter -> changeSessionFilter(event.filter)
            is HomeEvent.MoveCard -> moveCard(event.fromIndex, event.toIndex)
            is HomeEvent.StartDrag -> startDrag(event.index)
            is HomeEvent.UpdateDragPosition -> updateDragPosition(event.fromIndex, event.toIndex)
            is HomeEvent.EndDrag -> endDrag()
            is HomeEvent.DismissHeaderContent -> dismissHeaderContent()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { currentState ->
                val isFirstLoad = currentState !is ResourceState.Success
                if (isFirstLoad) {
                    ResourceState.Loading
                } else {
                    ResourceState.Success(
                        currentState.data.copy(isRefreshing = true)
                    )
                }
            }

            try {
                val currentData = _uiState.value.getDataOrNull()
                val sessionFilter = currentData?.selectedSessionFilter ?: SessionFilter.LAST_SESSION

                // ✅ PERFORMANCE: Use timeout and caching to prevent expensive operations
                val enhancedMetrics = try {
                    dashboardRepository.getEnhancedDashboardMetrics(sessionFilter)
                        .timeout(3.seconds) // 3 second timeout
                        .first()
                } catch (_: Exception) {
                    currentData?.enhancedMetrics
                }

                val performanceSnapshot = try {
                    getPerformanceMetricsUseCase()
                        .timeout(2.seconds) // 2 second timeout for lighter operation
                        .first()
                } catch (_: Exception) {
                    currentData?.performanceSnapshot
                }

                // Get header visibility state from internal preferences
                val isHeaderVisible = try {
                    manageHeaderContentStateUseCase.shouldShowHeaderContent().first()
                } catch (_: Exception) {
                    currentData?.isHeaderContentVisible ?: true
                }

                val homeUiData = HomeUiData(
                    enhancedMetrics = enhancedMetrics,
                    performanceSnapshot = performanceSnapshot,
                    selectedSessionFilter = sessionFilter,
                    cardOrder = currentData?.cardOrder ?: DashboardCardType.getAllCards(),
                    isDragging = currentData?.isDragging ?: false,
                    dragFromIndex = currentData?.dragFromIndex,
                    dragToIndex = currentData?.dragToIndex,
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis(),
                    isHeaderContentVisible = isHeaderVisible
                )

                _uiState.update { ResourceState.Success(homeUiData) }
            } catch (exception: Exception) {
                val currentData = _uiState.value.getDataOrNull()
                if (currentData != null) {
                    _uiState.update { 
                        ResourceState.Success(currentData.copy(isRefreshing = false)) 
                    }
                } else {
                    _uiState.update {
                        ResourceState.Error(exception, "Failed to load dashboard")
                    }
                }
            }
        }
    }

    private fun changeSessionFilter(filter: SessionFilter) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val currentData = _uiState.value.getDataOrNull()

                // Update the filter immediately
                _uiState.update { currentState ->
                    when (currentState) {
                        is ResourceState.Success -> {
                            ResourceState.Success(
                                currentState.data.copy(
                                    selectedSessionFilter = filter,
                                    isRefreshing = true
                                )
                            )
                        }

                        else -> currentState
                    }
                }

                // Load new data with the filter
                val enhancedMetrics = try {
                    dashboardRepository.getEnhancedDashboardMetrics(filter)
                        .timeout(3.seconds)
                        .first()
                } catch (_: Exception) {
                    null
                }

                val performanceSnapshot = try {
                    getPerformanceMetricsUseCase()
                        .timeout(2.seconds)
                        .first()
                } catch (_: Exception) {
                    currentData?.performanceSnapshot
                }

                // Get header visibility state
                val isHeaderVisible = try {
                    manageHeaderContentStateUseCase.shouldShowHeaderContent().first()
                } catch (_: Exception) {
                    currentData?.isHeaderContentVisible ?: true
                }

                val newData = HomeUiData(
                    enhancedMetrics = enhancedMetrics,
                    performanceSnapshot = performanceSnapshot,
                    selectedSessionFilter = filter,
                    cardOrder = currentData?.cardOrder ?: DashboardCardType.getAllCards(),
                    isDragging = false,
                    dragFromIndex = null,
                    dragToIndex = null,
                    isRefreshing = false,
                    lastUpdated = System.currentTimeMillis(),
                    isHeaderContentVisible = isHeaderVisible
                )

                _uiState.update { ResourceState.Success(newData) }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    when (currentState) {
                        is ResourceState.Success -> {
                            ResourceState.Success(currentState.data.copy(isRefreshing = false))
                        }

                        else -> ResourceState.Error(exception, "Failed to change session filter")
                    }
                }
            }
        }
    }

    private fun moveCard(fromIndex: Int, toIndex: Int) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    val currentOrder = currentState.data.cardOrder.toMutableList()
                    if (fromIndex in 0 until currentOrder.size && 
                        toIndex in 0 until currentOrder.size && 
                        fromIndex != toIndex) {
                        
                        currentOrder.add(toIndex, currentOrder.removeAt(fromIndex))
                        
                        ResourceState.Success(
                            currentState.data.copy(cardOrder = currentOrder)
                        )
                    } else {
                        currentState
                    }
                }
                else -> currentState
            }
        }
    }

    private fun startDrag(index: Int) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            isDragging = true,
                            dragFromIndex = index,
                            dragToIndex = index
                        )
                    )
                }

                else -> currentState
            }
        }
    }

    private fun updateDragPosition(fromIndex: Int, toIndex: Int) {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            dragFromIndex = fromIndex,
                            dragToIndex = toIndex
                        )
                    )
                }

                else -> currentState
            }
        }
    }

    private fun endDrag() {
        _uiState.update { currentState ->
            when (currentState) {
                is ResourceState.Success -> {
                    ResourceState.Success(
                        currentState.data.copy(
                            isDragging = false,
                            dragFromIndex = null,
                            dragToIndex = null
                        )
                    )
                }

                else -> currentState
            }
        }
    }

    private fun dismissHeaderContent() {
        viewModelScope.launch(ioDispatcher) {
            try {
                // Use the use case to dismiss header content
                manageHeaderContentStateUseCase.dismissHeaderContent()

                // Update UI state immediately to hide the header
                _uiState.update { currentState ->
                    when (currentState) {
                        is ResourceState.Success -> {
                            ResourceState.Success(
                                currentState.data.copy(isHeaderContentVisible = false)
                            )
                        }
                        else -> currentState
                    }
                }
            } catch (exception: Exception) {
                // Handle error gracefully - header will remain visible
                // Could log the error or show a toast if needed
            }
        }
    }
}