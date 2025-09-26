@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.presentation

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.domain.performance.PerformanceSnapshot
import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.internal.feature.home.domain.entity.DashboardCardType
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetricsMock
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import com.jarvis.core.internal.domain.performance.PerformanceSnapshotMock

typealias HomeUiState = ResourceState<HomeUiData>

data class HomeUiData(
    val enhancedMetrics: EnhancedDashboardMetrics? = null,
    val performanceSnapshot: PerformanceSnapshot? = null,
    val selectedSessionFilter: SessionFilter = SessionFilter.LAST_SESSION,
    val cardOrder: List<DashboardCardType> = DashboardCardType.getAllCards(),
    val isDragging: Boolean = false,
    val dragFromIndex: Int? = null,
    val dragToIndex: Int? = null,
    val isRefreshing: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isHeaderContentVisible: Boolean = true
) {
    companion object {
        val mockHomeUiData: HomeUiData
            get() = HomeUiData(
                enhancedMetrics = EnhancedDashboardMetricsMock.mockEnhancedDashboardMetrics,
                performanceSnapshot = PerformanceSnapshotMock.mockPerformanceSnapshot,
                selectedSessionFilter = SessionFilter.LAST_SESSION,
                cardOrder = DashboardCardType.getAllCards(),
                isDragging = false,
                isRefreshing = false,
                lastUpdated = System.currentTimeMillis(),
                isHeaderContentVisible = true
            )
    }
}

sealed interface HomeEvent {
    object RefreshDashboard : HomeEvent
    data class ChangeSessionFilter(val filter: SessionFilter) : HomeEvent
    data class MoveCard(val fromIndex: Int, val toIndex: Int) : HomeEvent
    data class StartDrag(val index: Int) : HomeEvent
    data class UpdateDragPosition(val fromIndex: Int, val toIndex: Int) : HomeEvent
    object EndDrag : HomeEvent
    object DismissHeaderContent : HomeEvent
}