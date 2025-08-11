package com.jarvis.features.inspector.presentation.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSDialog
import com.jarvis.core.designsystem.component.DSFilterChip
import com.jarvis.core.designsystem.component.DSPullToRefresh
import com.jarvis.core.designsystem.component.DSText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.EmptyContent
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.TransactionStatus

/**
 * Network Inspector screen route with state management
 */
@Composable
fun NetworkInspectorRoute(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
    viewModel: NetworkInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    NetworkInspectorScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorScreen(
    uiState: NetworkInspectorUiState,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
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
                    onNavigateToDetail = onNavigateToDetail
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
    modifier: Modifier = Modifier
) {
    DSPullToRefresh(
        isRefreshing = uiData.isRefreshing,
        onRefresh = { onEvent(NetworkInspectorEvent.RefreshTransactions) }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = DSJarvisTheme.spacing.m)
        ) {
            // Search and Filters
            NetworkInspectorFilters(
                uiData = uiData,
                onEvent = onEvent
            )

            Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))

            DSText(
                text = "Transactions",
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                modifier = modifier.padding(horizontal = DSJarvisTheme.spacing.m)
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(DSJarvisTheme.spacing.m)
            ) {
                // Transactions List
                if (uiData.transactions.isEmpty()) {
                    EmptyContent(
                        message = "No network transactions recorded yet",
                        actionText = "Refresh",
                        onAction = { onEvent(NetworkInspectorEvent.LoadTransactions) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
                    ) {
                        item { Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.xs)) }

                        items(uiData.transactions) { transaction ->
                            NetworkTransactionItem(
                                transaction = transaction,
                                onClick = {
                                    onNavigateToDetail(transaction.id)
                                }
                            )
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
    Column(modifier = modifier) {
        // Search Bar
        DSSearchBar(
            searchText = uiData.searchQuery,
            onValueChange = { onEvent(NetworkInspectorEvent.SearchQueryChanged(it)) },
            onTextClean = { onEvent(NetworkInspectorEvent.SearchQueryChanged("")) },
            modifier = Modifier
                .padding(horizontal = DSJarvisTheme.spacing.m)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))
        
        // Method Filters
        DSText(
            text = "HTTP Methods",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = modifier.padding(horizontal = DSJarvisTheme.spacing.m)
        )
        
        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DSJarvisTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            uiData.availableMethods.forEach { method ->
                val selected = uiData.selectedMethod == method
                val label = if (selected) "$method (${uiData.transactions.size})" else method
                DSFilterChip(
                    onClick = {
                        val newMethod = if (selected) null else method
                        onEvent(NetworkInspectorEvent.MethodFilterChanged(newMethod))
                    },
                    label = label,
                    selected = selected
                )
            }
        }

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.m))

        // Status Filters
        DSText(
            text = "Status",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium,
            modifier = modifier.padding(horizontal = DSJarvisTheme.spacing.m)
        )

        Spacer(modifier = Modifier.height(DSJarvisTheme.dimensions.s))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = DSJarvisTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            uiData.availableStatuses.forEach { status ->
                val selected = uiData.selectedStatus == status
                val label = if (selected) "$status (${uiData.transactions.size})" else status
                DSFilterChip(
                    onClick = {
                        val newStatus = if (selected) null else status
                        onEvent(NetworkInspectorEvent.StatusFilterChanged(newStatus))
                    },
                    label = label,
                    selected = selected
                )
            }
        }
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
        elevation = DSJarvisTheme.elevations.level2,
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

// Removed duplicate state components - now using BaseScreenComponents.ResourceStateContent

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
            onNavigateToDetail = {}
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
            onNavigateToDetail = {}
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
            onNavigateToDetail = {}
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
            onNavigateToDetail = {}
        )
    }
}