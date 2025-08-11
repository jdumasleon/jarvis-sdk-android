package com.jarvis.features.home.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.features.home.domain.entity.DashboardMetrics
import com.jarvis.features.home.domain.entity.NetworkMetrics
import com.jarvis.features.home.domain.entity.PerformanceMetrics
import com.jarvis.features.home.domain.entity.PerformanceRating
import com.jarvis.features.home.domain.entity.PreferencesMetrics

/**
 * Home screen route with state management
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

/**
 * Home screen composable following ResourceState pattern
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(DSJarvisTheme.spacing.m)
            .fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(HomeEvent.RefreshDashboard) },
            loadingMessage = "Loading dashboard...",
            emptyMessage = "No data available",
            modifier = Modifier.weight(1f)
        ) { uiData ->
            HomeContent(
                uiData = uiData,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiData: HomeUiData,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    DSPullToRefresh(
        isRefreshing = uiData.isRefreshing,
        onRefresh = { onEvent(HomeEvent.RefreshDashboard) }
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            item { Spacer(Modifier.height(DSJarvisTheme.spacing.xs)) }

            item {
                DashboardOverview(
                    dashboardMetrics = uiData.dashboardMetrics
                )
            }

            item {
                NetworkMetricsCard(
                    networkMetrics = uiData.dashboardMetrics.networkMetrics
                )
            }

            item {
                PreferencesMetricsCard(
                    preferencesMetrics = uiData.dashboardMetrics.preferencesMetrics
                )
            }

            item {
                PerformanceMetricsCard(
                    performanceMetrics = uiData.dashboardMetrics.performanceMetrics
                )
            }

            item { Spacer(Modifier.height(DSJarvisTheme.spacing.m)) }
        }
    }
}

@Composable
private fun DashboardOverview(
    dashboardMetrics: DashboardMetrics,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level3,
        parallaxEnabled = true
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSText(
                text = "Quick Overview",
                style = DSJarvisTheme.typography.heading.heading5,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewItem(
                    title = "Network Calls",
                    value = "${dashboardMetrics.networkMetrics.totalCalls}",
                    subtitle = "Total",
                    icon = Icons.Default.NetworkCheck
                )

                OverviewItem(
                    title = "Preferences",
                    value = "${dashboardMetrics.preferencesMetrics.totalPreferences}",
                    subtitle = "Total",
                    icon = Icons.Default.Settings,
                )
            }
        }
    }
}

@Composable
private fun OverviewItem(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier.padding(DSJarvisTheme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        DSIcon(
            imageVector = icon,
            tint = DSJarvisTheme.colors.primary.primary100,
            contentDescription = title
        )
        DSText(
            text = value,
            style = DSJarvisTheme.typography.heading.heading2,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
        DSText(
            text = title,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )
        DSText(
            text = subtitle,
            style = DSJarvisTheme.typography.body.small,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

@Composable
private fun NetworkMetricsCard(
    networkMetrics: NetworkMetrics,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level3,
        parallaxEnabled = true
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSText(
                text = "Network Metrics",
                style = DSJarvisTheme.typography.heading.heading5,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            MetricRow("Total Calls:", "${networkMetrics.totalCalls}")
            MetricRow("Average Speed:", "${networkMetrics.averageSpeed.toInt()} ms")
            MetricRow(
                "Success Rate:",
                "${String.format("%.1f", networkMetrics.successRate)}%"
            )

            MetricRow("Average Request Size:", "${networkMetrics.averageRequestSize} B")
            MetricRow("Average Response Size:", "${networkMetrics.averageResponseSize} B")

            MetricRow("p50:", "${networkMetrics.p50.toInt()} ms")
            MetricRow("p90:", "${networkMetrics.p90.toInt()} ms")
            MetricRow("p95:", "${networkMetrics.p95.toInt()} ms")
            MetricRow("p99:", "${networkMetrics.p99.toInt()} ms")

            networkMetrics.mostUsedEndpoint?.let { endpoint ->
                MetricRow("Most Used:", endpoint)
            }

            if (networkMetrics.topSlowEndpoints.isNotEmpty()) {
                DSText(
                    text = "Top Slow Endpoints (p95):",
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral80
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
                ) {
                    networkMetrics.topSlowEndpoints.forEachIndexed { index, endpoint ->
                        MetricRow("${index + 1}.", endpoint)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferencesMetricsCard(
    preferencesMetrics: PreferencesMetrics,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level3,
        parallaxEnabled = true
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = "Preferences Metrics",
                    style = DSJarvisTheme.typography.heading.heading5,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }

            MetricRow("Total Preferences:", "${preferencesMetrics.totalPreferences}")
            preferencesMetrics.mostCommonType?.let { type ->
                MetricRow("Most Common:", type)
            }
            preferencesMetrics.preferencesByType.entries.take(2).forEach { (type, count) ->
                MetricRow(type, "$count")
            }
        }
    }
}

@Composable
private fun PerformanceMetricsCard(
    performanceMetrics: PerformanceMetrics,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level3,
        parallaxEnabled = true
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSText(
                text = "Performance Metrics",
                style = DSJarvisTheme.typography.heading.heading5,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            val ratingColor = when (performanceMetrics.overallRating) {
                PerformanceRating.EXCELLENT -> DSJarvisTheme.colors.success.success100
                PerformanceRating.GOOD -> DSJarvisTheme.colors.primary.primary100
                PerformanceRating.AVERAGE -> DSJarvisTheme.colors.warning.warning100
                PerformanceRating.POOR -> DSJarvisTheme.colors.error.error80
                PerformanceRating.CRITICAL -> DSJarvisTheme.colors.error.error100
            }

            MetricRow("Overall Rating:", performanceMetrics.overallRating.name, valueColor = ratingColor)
            MetricRow("Avg Response Time:", "${performanceMetrics.averageResponseTime.toInt()} ms")
            MetricRow("p95:", "${performanceMetrics.p95.toInt()} ms")
            MetricRow("Apdex:", String.format("%.2f", performanceMetrics.apdex))
            MetricRow("Error Rate:", "${String.format("%.1f", performanceMetrics.errorRate)}%")

            performanceMetrics.slowestCall?.let { slowest ->
                MetricRow("Slowest Call:", "${slowest.toInt()} ms")
            }
            performanceMetrics.fastestCall?.let { fastest ->
                MetricRow("Fastest Call:", "${fastest.toInt()} ms")
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DSJarvisTheme.colors.neutral.neutral100
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DSText(
            text = label,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral80
        )
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = com.jarvis.core.presentation.state.ResourceState.Success(HomeMockData.mockHomeUiData),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun HomeScreenLoadingPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = com.jarvis.core.presentation.state.ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun HomeScreenErrorPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = com.jarvis.core.presentation.state.ResourceState.Error(
                Exception("Network error"),
                "Failed to load dashboard"
            ),
            onEvent = {}
        )
    }
}