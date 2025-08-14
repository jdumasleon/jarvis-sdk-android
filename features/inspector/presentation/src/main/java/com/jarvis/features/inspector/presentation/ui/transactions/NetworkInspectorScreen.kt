package com.jarvis.features.inspector.presentation.ui.transactions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonSize
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSDialog
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.EmptyContent
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.TransactionStatus
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import com.jarvis.features.inspector.presentation.ui.components.NetworkTransactionGroupHeader
import com.jarvis.features.inspector.presentation.ui.components.TODAY
import com.jarvis.features.inspector.presentation.ui.components.groupByDate

/**
 * Network Inspector screen route with state management
 */
@Composable
fun NetworkInspectorRoute(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRules: () -> Unit,
    viewModel: NetworkInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NetworkInspectorScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToRules = onNavigateToRules
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorScreen(
    uiState: NetworkInspectorUiState,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(top = DSJarvisTheme.spacing.m)
            .fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            onRetry = { onEvent(NetworkInspectorEvent.LoadTransactions) },
            onDismiss = { onEvent(NetworkInspectorEvent.ClearError) },
            loadingMessage = "Loading transactions...",
            emptyMessage = "No network transactions recorded yet",
            emptyActionText = "Load Transactions",
            onEmptyAction = { onEvent(NetworkInspectorEvent.LoadTransactions) }
        ) { uiData ->
            NetworkInspectorContent(
                uiData = uiData,
                onEvent = onEvent,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToRules = onNavigateToRules
            )
        }

        // Confirmation Dialog
        if (uiState is ResourceState.Success && uiState.data.showClearConfirmation) {
            ClearConfirmationDialog(
                onConfirm = {
                    onEvent(NetworkInspectorEvent.ClearAllTransactions)
                    onEvent(NetworkInspectorEvent.ShowClearConfirmation(false))
                },
                onDismiss = { onEvent(NetworkInspectorEvent.ShowClearConfirmation(false)) }
            )
        }
    }
}

@Composable
private fun NetworkInspectorContent(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var isFiltersVisible by rememberSaveable { mutableStateOf(true) }
    var filtersHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    // Optimized nested scroll connection with debouncing
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private val threshold = 8f
            private var accumulatedDelta = 0f

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && filtersHeightPx > 0) {
                    val dy = available.y
                    accumulatedDelta += dy

                    // Only trigger state change when threshold is exceeded
                    when {
                        accumulatedDelta < -threshold && isFiltersVisible -> {
                            isFiltersVisible = false
                            accumulatedDelta = 0f
                        }
                        accumulatedDelta > threshold && !isFiltersVisible -> {
                            isFiltersVisible = true
                            accumulatedDelta = 0f
                        }
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (filtersHeightPx > 0) {
                    when {
                        available.y < -200 && isFiltersVisible -> isFiltersVisible = false
                        available.y > 200 && !isFiltersVisible -> isFiltersVisible = true
                    }
                }
                accumulatedDelta = 0f
                return Velocity.Zero
            }
        }
    }

    // Animated progress with optimized spring animation
    val filtersProgress by animateFloatAsState(
        targetValue = if (isFiltersVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 250,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "filtersProgress"
    )

    // Calculate animated offset
    val filtersOffsetPx = remember(filtersProgress, filtersHeightPx) {
        -filtersHeightPx * (1f - filtersProgress)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Animated Filters Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = filtersOffsetPx
                    alpha = filtersProgress.coerceIn(0f, 1f)
                }
                .onGloballyPositioned { coordinates ->
                    val newHeight = coordinates.size.height
                    if (newHeight != filtersHeightPx && newHeight > 0) {
                        filtersHeightPx = newHeight
                    }
                }
        ) {
            NetworkInspectorFilters(
                uiData = uiData,
                onEvent = onEvent
            )
        }

        // Content below filters with dynamic height adjustment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = with(density) {
                        (filtersHeightPx * filtersProgress).toInt().toDp()
                    }
                )
        ) {
            InspectorActions(
                uiData = uiData,
                onEvent = onEvent,
                onNavigateToRules = onNavigateToRules
            )

            DSPullToRefresh(
                isRefreshing = uiData.isRefreshing,
                onRefresh = { onEvent(NetworkInspectorEvent.RefreshTransactions) }
            ) {
                if (uiData.transactions.isEmpty()) {
                    EmptyContent(
                        message = "No network transactions recorded yet",
                        actionText = "Refresh",
                        onAction = { onEvent(NetworkInspectorEvent.LoadTransactions) },
                        modifier = Modifier.padding(DSJarvisTheme.spacing.m)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = DSJarvisTheme.spacing.m),
                        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                    ) {
                        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs)) }

                        val groupedTransactions = uiData.transactions.groupByDate()
                        
                        groupedTransactions.forEach { group ->
                            // Date group header
                            item(key = "header_${group.date}") {
                                if (group.date != TODAY) {
                                    NetworkTransactionGroupHeader(
                                        title = group.date,
                                        transactionCount = group.transactions.size
                                    )
                                }
                            }
                            
                            // Transactions in this group
                            items(
                                items = group.transactions,
                                key = { transaction -> transaction.id }
                            ) { transaction ->
                                NetworkTransactionItem(
                                    transaction = transaction,
                                    onClick = { onNavigateToDetail(transaction.id) }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkInspectorFilters(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
    ) {
        DSSearchBar(
            searchText = uiData.searchQuery,
            onValueChange = { onEvent(NetworkInspectorEvent.SearchQueryChanged(it)) },
            onTextClean = { onEvent(NetworkInspectorEvent.SearchQueryChanged("")) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSJarvisTheme.spacing.m)
        )

        MethodsTypesChips(
            uiData = uiData,
            onEvent = onEvent
        )

        StatusTypesChips(
            uiData = uiData,
            onEvent = onEvent
        )
    }
}

@Composable
private fun MethodsTypesChips(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit
) {
    Column (
        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "HTTP Methods",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.s)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            DSFilterChip(
                selected = uiData.selectedMethod == null,
                onClick = { onEvent(NetworkInspectorEvent.MethodFilterChanged(null)) },
                label = "All"
            )

            uiData.availableMethods.forEach { method ->
                val selected = uiData.selectedMethod == method
                DSFilterChip(
                    onClick = {
                        val newMethod = if (selected) null else method
                        onEvent(NetworkInspectorEvent.MethodFilterChanged(newMethod))
                    },
                    label = method,
                    selected = selected
                )
            }
        }
    }
}

@Composable
private fun StatusTypesChips(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m)
    ) {
        DSText(
            text = "Status",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = DSJarvisTheme.spacing.s)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            uiData.availableStatuses.forEach { status ->
                val selected = uiData.selectedStatus == status
                DSFilterChip(
                    onClick = {
                        val newStatus = if (selected) null else status
                        onEvent(NetworkInspectorEvent.StatusFilterChanged(newStatus))
                    },
                    label = status,
                    selected = selected
                )
            }
        }
    }
}

@Composable
private fun InspectorActions(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToRules: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DSJarvisTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DSText(
            text = "Transactions (${uiData.transactions.size})",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = DSJarvisTheme.spacing.m)
        )

        DSButton(
            text = "Rules",
            onClick = onNavigateToRules,
            style = DSButtonStyle.TEXT,
            size = DSButtonSize.EXTRA_SMALL
        )
    }
}

@Composable
private fun NetworkTransactionItem(
    transaction: NetworkTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DSCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        shape = DSJarvisTheme.shapes.m,
        elevation = DSJarvisTheme.elevations.level1,
    ) {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.s),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DSText(
                    text = transaction.request.method.name,
                    style = DSJarvisTheme.typography.body.small,
                    color = getMethodColor(transaction.request.method.name)
                )

                DSText(
                    text = getStatusText(transaction),
                    style = DSJarvisTheme.typography.body.small,
                    color = getStatusColor(transaction.status)
                )
            }

            Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs))

            DSText(
                text = transaction.request.url,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DSText(
                    text = formatTimestamp(transaction.startTime),
                    style = DSJarvisTheme.typography.body.small,
                    color = DSJarvisTheme.colors.neutral.neutral60
                )

                if (transaction.endTime != null) {
                    val responseTime = transaction.endTime!! - transaction.startTime
                    DSText(
                        text = "${responseTime}ms",
                        style = DSJarvisTheme.typography.body.small,
                        color = DSJarvisTheme.colors.neutral.neutral60
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DSDialog(
        onDismissRequest = onDismiss,
        title = { DSText("Clear All Transactions") },
        text = { DSText("Are you sure you want to clear all network transactions? This action cannot be undone.") },
        confirmButton = {
            DSButton(
                text = "Clear All",
                style = DSButtonStyle.PRIMARY,
                onClick = onConfirm
            )
        },
        dismissButton = {
            DSButton(
                text = "Cancel",
                style = DSButtonStyle.SECONDARY,
                onClick = onDismiss
            )
        }
    )
}

// Helper functions
private fun getMethodColor(method: String): Color {
    return when (method) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }
}

private fun getStatusColor(status: TransactionStatus): Color {
    return when (status) {
        TransactionStatus.COMPLETED -> Color(0xFF4CAF50)
        TransactionStatus.FAILED -> Color(0xFFF44336)
        TransactionStatus.PENDING -> Color(0xFFFF9800)
    }
}

private fun getStatusText(transaction: NetworkTransaction): String {
    return when (transaction.status) {
        TransactionStatus.COMPLETED -> transaction.response?.statusCode?.toString() ?: "200"
        TransactionStatus.FAILED -> "Failed"
        TransactionStatus.PENDING -> "Pending"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 1000 -> "Just now"
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        else -> "${diff / 3600000}h ago"
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun NetworkInspectorScreenPreview() {
    DSJarvisTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Success(NetworkInspectorUiData.mockNetworkInspectorUiData),
            onEvent = {},
            onNavigateToDetail = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkInspectorScreenLoadingPreview() {
    DSJarvisTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Loading,
            onEvent = {},
            onNavigateToDetail = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkInspectorScreenEmptyPreview() {
    DSJarvisTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Success(NetworkInspectorUiData()),
            onEvent = {},
            onNavigateToDetail = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NetworkInspectorScreenErrorPreview() {
    DSJarvisTheme {
        NetworkInspectorScreen(
            uiState = ResourceState.Error(
                exception = Exception("Network error"),
                message = "Failed to load transactions"
            ),
            onEvent = {},
            onNavigateToDetail = {},
            onNavigateToRules = {}
        )
    }
}
