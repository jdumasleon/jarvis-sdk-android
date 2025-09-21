package com.jarvis.internal.feature.home.presentation

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.jarvis.library.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSReorderableItem
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.rememberReorderableLazyStaggeredGridState
import com.jarvis.core.designsystem.component.DSIconTint
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.internal.feature.home.domain.entity.DashboardCardType
import com.jarvis.internal.feature.home.domain.entity.SessionFilter
import com.jarvis.internal.feature.home.presentation.components.NetworkAreaChart
import com.jarvis.internal.feature.home.presentation.components.PreferencesOverviewChart
import com.jarvis.internal.feature.home.presentation.components.SessionFilterChip
import com.jarvis.internal.feature.home.presentation.components.SlowestEndpointsList
import com.jarvis.internal.feature.home.presentation.components.TopEndpointsBarChart
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetrics
import com.jarvis.internal.feature.home.domain.entity.EnhancedDashboardMetricsMock
import com.jarvis.core.designsystem.component.DSFlag
import com.jarvis.core.designsystem.component.FlagStyle
import com.jarvis.core.designsystem.component.DSIconButton
import com.jarvis.core.domain.performance.PerformanceSnapshot
import com.jarvis.core.domain.performance.PerformanceSnapshotMock
import com.jarvis.internal.feature.home.presentation.components.HealthSummary
import com.jarvis.internal.feature.home.presentation.components.HttpMethodsWithDetails
import com.jarvis.internal.feature.home.presentation.components.PerformanceOverviewCharts

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
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // Optimized nested scroll connection with debouncing
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                when {
                    delta < -50f && isHeaderVisible -> {
                        isHeaderVisible = false
                    }
                    delta > 50f && !isHeaderVisible -> {
                        isHeaderVisible = true
                    }
                }
                
                return Offset.Zero
            }
        }
    }

    val headerProgress = animateFloatAsState(
        targetValue = if (isHeaderVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "HeaderAnimation"
    ).value

    val headerOffsetPx = (-headerHeightPx * (1f - headerProgress))

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Animated Header Section (DSFlag + Filters)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = headerOffsetPx
                    alpha = headerProgress.coerceIn(0f, 1f)
                }
                .onGloballyPositioned { coordinates ->
                    val newHeight = coordinates.size.height
                    if (newHeight != headerHeightPx && newHeight > 0) {
                        headerHeightPx = newHeight
                    }
                }
        ) {
            HeaderContent(
                selectedFilter = uiData.selectedSessionFilter,
                onFilterChange = { onEvent(HomeEvent.ChangeSessionFilter(it)) }
            )
        }

        // Content below header with dynamic height adjustment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(density) {
                        (headerHeightPx * headerProgress).toInt().toDp()
                    }
                )
        ) {
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
}

@Composable
private fun HeaderContent(
    selectedFilter: SessionFilter,
    onFilterChange: (SessionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Wealth-themed information flag
        DSFlag(
            title = stringResource(R.string.features_home_presentation_wealth_dashboard),
            description = stringResource(R.string.features_home_presentation_wealth_dashboard_description),
            style = FlagStyle.Info,
            closable = true,
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m)
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
        
        // Top bar with session filter
        DateFilterTypesChips(
            selectedFilter = selectedFilter,
            onFilterChange = onFilterChange,
        )
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
            color = DSJarvisTheme.colors.neutral.neutral100
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(bottom = DSJarvisTheme.spacing.s),
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
    enhancedMetrics: EnhancedDashboardMetrics?,
    performanceSnapshot: PerformanceSnapshot?,
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
        modifier = modifier
            .padding(bottom = DSJarvisTheme.spacing.m)
            .fillMaxSize()
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
                                    tint = DSIconTint.Solid(DSJarvisTheme.colors.neutral.neutral60),
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

                            key(cardType.name, enhancedMetrics?.lastUpdated, performanceSnapshot?.timestamp) {
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
}

@Composable
private fun DashboardCardContent(
    cardType: DashboardCardType,
    enhancedMetrics: EnhancedDashboardMetrics?,
    performanceSnapshot: PerformanceSnapshot?,
    modifier: Modifier = Modifier
) {
    Column {
        when (cardType) {
            DashboardCardType.HEALTH_SUMMARY -> {
                enhancedMetrics?.healthScore?.let { healthScore ->
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))
                    HealthSummary(
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
            uiState = ResourceState.Success(HomeUiData.Companion.mockHomeUiData),
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
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenDarkThemePreview() {
    DSJarvisTheme(darkTheme = true) {
        HomeScreen(
            uiState = ResourceState.Success(HomeUiData.Companion.mockHomeUiData),
            onEvent = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Theme - Error",
    uiMode = Configuration.UI_MODE_NIGHT_YES
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
    uiMode = Configuration.UI_MODE_NIGHT_YES
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
                enhancedMetrics = EnhancedDashboardMetricsMock.mockEnhancedDashboardMetrics,
                performanceSnapshot = PerformanceSnapshotMock.mockPerformanceSnapshot,
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
                enhancedMetrics = EnhancedDashboardMetricsMock.mockEnhancedDashboardMetrics,
                performanceSnapshot = PerformanceSnapshotMock.mockPerformanceSnapshot,
                modifier = Modifier.padding(DSJarvisTheme.spacing.m)
            )
        }
    }
}