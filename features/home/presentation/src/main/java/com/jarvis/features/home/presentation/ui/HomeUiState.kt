package com.jarvis.features.home.presentation.ui

import com.jarvis.core.domain.performance.PerformanceSnapshot
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.entity.NetworkMetrics
import com.jarvis.features.home.domain.entity.PerformanceMetrics
import com.jarvis.features.home.domain.entity.PerformanceRating
import com.jarvis.features.home.domain.entity.PreferencesMetrics

/**
 * UI State for Home screen using ResourceState pattern
 */
typealias HomeUiState = ResourceState<HomeUiData>

/**
 * UI Data for Home screen
 */
data class HomeUiData(
    val dashboardMetrics: DashboardMetrics,
    val performanceSnapshot: PerformanceSnapshot? = null,
    val selectedTab: HomeTab = HomeTab.DASHBOARD,
    val isRefreshing: Boolean = false
)

/**
 * Events that can be triggered from Home screen
 */
sealed interface HomeEvent {
    data object RefreshDashboard : HomeEvent
    data class TabSelected(val tab: HomeTab) : HomeEvent
    data object NavigateToInspector : HomeEvent
    data object NavigateToPreferences : HomeEvent
    data object ClearError : HomeEvent
}

/**
 * Navigation tabs for Home screen
 */
enum class HomeTab {
    DASHBOARD,
    INSPECTOR,
    PREFERENCES
}

/**
 * Mock data for previews and testing
 */
object HomeMockData {

    val mockNetworkMetrics = NetworkMetrics(
        totalCalls = 247,
        averageSpeed = 1200.0,
        successfulCalls = 235,
        failedCalls = 12,
        successRate = 95.1,
        averageRequestSize = 1024L,
        averageResponseSize = 4096L,
        p50 = 800.0,
        p90 = 1500.0,
        p95 = 2000.0,
        p99 = 2800.0,
        mostUsedEndpoint = "/api/v1/users",
        topSlowEndpoints = listOf(
            "/api/v1/reports",
            "/api/v1/analytics",
            "/api/v1/orders"
        )
    )

    val mockPreferencesMetrics = PreferencesMetrics(
        totalPreferences = 45,
        preferencesByType = mapOf(
            "SharedPreferences" to 25,
            "DataStore" to 15,
            "Proto DataStore" to 5
        ),
        mostCommonType = "SharedPreferences",
        lastModified = System.currentTimeMillis()
    )

    val mockPerformanceMetrics = PerformanceMetrics(
        overallRating = PerformanceRating.GOOD,
        averageResponseTime = 1200.0,
        slowestCall = 3500.0,
        fastestCall = 150.0,
        errorRate = 4.8,
        p95 = 2000.0,
        apdex = 0.85
    )

    val mockDashboardMetrics = DashboardMetrics(
        networkMetrics = mockNetworkMetrics,
        preferencesMetrics = mockPreferencesMetrics,
        performanceMetrics = mockPerformanceMetrics
    )

    val mockHomeUiData = HomeUiData(
        dashboardMetrics = mockDashboardMetrics
    )
}