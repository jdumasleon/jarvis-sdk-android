package com.jarvis.features.home.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSReorderableItem
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.rememberReorderableLazyStaggeredGridState
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.home.domain.entity.DashboardCardType
import com.jarvis.features.home.domain.entity.SessionFilter
import com.jarvis.features.home.presentation.ui.components.HealthScoreGauge
import com.jarvis.features.home.presentation.ui.components.HttpMethodsDonutChart
import com.jarvis.features.home.presentation.ui.components.NetworkAreaChart
import com.jarvis.features.home.presentation.ui.components.PreferencesOverviewChart
import com.jarvis.features.home.presentation.ui.components.SessionFilterChip
import com.jarvis.features.home.presentation.ui.components.SlowestEndpointsList
import com.jarvis.features.home.presentation.ui.components.TopEndpointsBarChart
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSIconButton
import com.jarvis.features.home.presentation.R
import com.jarvis.features.home.presentation.ui.components.HttpMethodsCard
import com.jarvis.features.home.presentation.ui.components.HttpMethodsWithDetails
import com.jarvis.features.home.presentation.ui.components.PerformanceOverviewCharts

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = DSJarvisTheme.spacing.m)
            .fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(HomeEvent.RefreshDashboard) },
            onDismiss = { /* Handle error dismissal if needed */ },
            loadingMessage = "Loading analytics dashboard...",
            emptyMessage = "No analytics data available",
            emptyActionText = "Refresh Dashboard",
            onEmptyAction = { onEvent(HomeEvent.RefreshDashboard) }
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
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar with session filter
        DateFilterTypesChips(
            selectedFilter = uiData.selectedSessionFilter,
            onFilterChange = { onEvent(HomeEvent.ChangeSessionFilter(it)) },
        )

        // Dashboard cards with drag & drop
        DSPullToRefresh(
            isRefreshing = uiData.isRefreshing,
            onRefresh = { onEvent(HomeEvent.RefreshDashboard) }
        ) {
            DraggableCardGrid(
                cardOrder = uiData.cardOrder,
                enhancedMetrics = uiData.enhancedMetrics,
                performanceSnapshot = uiData.performanceSnapshot,
                onCardMove = { fromIndex, toIndex ->
                    onEvent(HomeEvent.MoveCard(fromIndex, toIndex))
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DateFilterTypesChips(
    selectedFilter: SessionFilter,
    onFilterChange: (SessionFilter) -> Unit
) {
    Column (
        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
    ) {
        DSText(
            text = stringResource(R.string.filter_label).uppercase(),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100,
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            SessionFilter.entries.forEach { filter ->
                SessionFilterChip(
                    filter = filter,
                    isSelected = selectedFilter == filter,
                    onSelected = onFilterChange
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DraggableCardGrid(
    cardOrder: List<DashboardCardType>,
    enhancedMetrics: com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics?,
    performanceSnapshot: com.jarvis.core.domain.performance.PerformanceSnapshot?,
    onCardMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val reorderableLazyStaggeredGridState =
        rememberReorderableLazyStaggeredGridState(lazyStaggeredGridState) { from, to ->
            onCardMove(from.index, to.index)
            haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

    LazyVerticalStaggeredGrid(
        state = lazyStaggeredGridState,
        columns = StaggeredGridCells.Adaptive(300.dp),
        contentPadding = PaddingValues(DSJarvisTheme.spacing.m),
        verticalItemSpacing = DSJarvisTheme.spacing.m,
        horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
        modifier = modifier.fillMaxSize()
    ) {
        itemsIndexed(cardOrder, key = { _, cardType -> cardType.name }) { index, cardType ->
            DSReorderableItem(
                state = reorderableLazyStaggeredGridState,
                key = cardType.name,
                modifier = Modifier.fillMaxWidth()
            ) { isDragging ->
                val interactionSource = remember { MutableInteractionSource() }

                Column (
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                ) {
                    DSText(
                        modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
                        text = cardType.title.uppercase(),
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral100
                    )

                    DSCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = DSJarvisTheme.shapes.m,
                        elevation = if (isDragging) DSJarvisTheme.elevations.level2 else DSJarvisTheme.elevations.level1

                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    customActions = listOf(
                                        CustomAccessibilityAction(
                                            label = "Mover antes",
                                            action = {
                                                if (index > 0) {
                                                    val from = index
                                                    val to = index - 1
                                                    onCardMove(from, to)
                                                    true
                                                } else false
                                            }
                                        ),
                                        CustomAccessibilityAction(
                                            label = "Mover después",
                                            action = {
                                                if (index < cardOrder.size - 1) {
                                                    val from = index
                                                    val to = index + 1
                                                    onCardMove(from, to)
                                                    true
                                                } else false
                                            }
                                        ),
                                    )
                                }
                                .padding(horizontal = DSJarvisTheme.spacing.s)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DSText(
                                    text = cardType.description,
                                    style = DSJarvisTheme.typography.body.small,
                                    color = DSJarvisTheme.colors.neutral.neutral60
                                )

                                DSIconButton(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = "Reordering",
                                    tint = DSJarvisTheme.colors.neutral.neutral60,
                                    modifier = Modifier
                                        .size(DSJarvisTheme.spacing.l)
                                        .draggableHandle(
                                            onDragStarted = {
                                                haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                            },
                                            onDragStopped = {
                                                haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                            },
                                            interactionSource = interactionSource
                                        )
                                        .clearAndSetSemantics { },
                                    onClick = { /* handle-only */ },
                                )
                            }

                            DashboardCardContent(
                                cardType = cardType,
                                enhancedMetrics = enhancedMetrics,
                                performanceSnapshot = performanceSnapshot,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun DashboardCardContent(
    cardType: DashboardCardType,
    enhancedMetrics: com.jarvis.features.home.domain.entity.EnhancedDashboardMetrics?,
    performanceSnapshot: com.jarvis.core.domain.performance.PerformanceSnapshot?,
    modifier: Modifier = Modifier
) {
    Column {
        when (cardType) {
            DashboardCardType.HEALTH_SUMMARY -> {
                enhancedMetrics?.healthScore?.let { healthScore ->
                    HealthScoreGauge(
                        healthScore = healthScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = DSJarvisTheme.spacing.m,
                                start = DSJarvisTheme.spacing.m,
                                end = DSJarvisTheme.spacing.m
                            )
                    )
                }
            }

            DashboardCardType.PERFORMANCE_METRICS -> {
                performanceSnapshot?.let { snapshot ->
                    PerformanceOverviewCharts(
                        performanceSnapshot = snapshot,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = DSJarvisTheme.spacing.m,
                                start = DSJarvisTheme.spacing.m,
                                end = DSJarvisTheme.spacing.m,
                                bottom = DSJarvisTheme.spacing.m
                            )
                    )
                }
            }

            DashboardCardType.NETWORK_OVERVIEW -> {
                enhancedMetrics?.enhancedNetworkMetrics?.let { networkMetrics ->
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                    NetworkAreaChart(
                        dataPoints = networkMetrics.requestsOverTime,
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            }

            DashboardCardType.PREFERENCES_OVERVIEW -> {
                enhancedMetrics?.enhancedPreferencesMetrics?.let { prefsMetrics ->
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                    PreferencesOverviewChart(
                        preferencesMetrics = prefsMetrics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            DashboardCardType.HTTP_METHODS -> {
                enhancedMetrics?.enhancedNetworkMetrics?.let { networkMetrics ->
                    HttpMethodsWithDetails(
                        httpMethods = networkMetrics.httpMethodDistribution,
                        inline = true,
                    )
                }
            }

            DashboardCardType.TOP_ENDPOINTS -> {
                enhancedMetrics?.enhancedNetworkMetrics?.let { networkMetrics ->
                    TopEndpointsBarChart(
                        endpoints = networkMetrics.topEndpoints,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            DashboardCardType.SLOW_ENDPOINTS -> {
                enhancedMetrics?.enhancedNetworkMetrics?.let { networkMetrics ->
                    SlowestEndpointsList(
                        slowEndpoints = networkMetrics.slowestEndpoints,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ----- Previews -----

@Preview(showBackground = true, name = "Success - With Analytics Data", heightDp = 6800)
@Composable
private fun HomeScreenSuccessPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.mockHomeUiData),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - Empty State")
@Composable
private fun HomeScreenEmptyPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData()),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun HomeScreenLoadingPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Loading,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle State")
@Composable
private fun HomeScreenIdlePreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Idle,
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun HomeScreenErrorPreview() {
    DSJarvisTheme {
        HomeScreen(
            uiState = ResourceState.Error(
                exception = Exception("Network connection failed"),
                message = "Failed to load dashboard analytics"
            ),
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme - Success",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenDarkThemePreview() {
    DSJarvisTheme(darkTheme = true) {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.mockHomeUiData),
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme - Error",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenDarkThemeErrorPreview() {
    DSJarvisTheme(darkTheme = true) {
        HomeScreen(
            uiState = ResourceState.Error(
                exception = Exception("Network connection failed"),
                message = "Failed to load dashboard analytics"
            ),
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme - Single Card",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DashboardCardDarkPreview() {
    DSJarvisTheme(darkTheme = true) {
        DSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = DSJarvisTheme.elevations.level1
        ) {
            DashboardCardContent(
                cardType = DashboardCardType.HEALTH_SUMMARY,
                enhancedMetrics = com.jarvis.features.home.domain.entity.EnhancedDashboardMetricsMock.mockEnhancedDashboardMetrics,
                performanceSnapshot = com.jarvis.core.domain.performance.PerformanceSnapshotMock.mockPerformanceSnapshot,
                modifier = Modifier.padding(DSJarvisTheme.spacing.m)
            )
        }
    }
}

@Preview(showBackground = true, name = "Single Dashboard Card")
@Composable
private fun DashboardCardPreview() {
    DSJarvisTheme {
        DSCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = DSJarvisTheme.elevations.level1
        ) {
            DashboardCardContent(
                cardType = DashboardCardType.HEALTH_SUMMARY,
                enhancedMetrics = com.jarvis.features.home.domain.entity.EnhancedDashboardMetricsMock.mockEnhancedDashboardMetrics,
                performanceSnapshot = com.jarvis.core.domain.performance.PerformanceSnapshotMock.mockPerformanceSnapshot,
                modifier = Modifier.padding(DSJarvisTheme.spacing.m)
            )
        }
    }
}