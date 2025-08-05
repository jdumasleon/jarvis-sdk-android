package com.jarvis.features.inspector.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.jarvis.core.designsystem.component.DSButton
import com.jarvis.core.designsystem.component.DSButtonStyle
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSCircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jarvis.core.designsystem.component.DSSearchBar
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.TransactionStatus

@Composable
fun NetworkInspectorRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NetworkInspectorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    NetworkInspectorScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkInspectorScreen(
    uiState: NetworkInspectorUiState,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { DSText("Network Inspector") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        DSIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(NetworkInspectorEvent.ShowClearConfirmation(true)) }) {
                        DSIcon(Icons.Default.Clear, contentDescription = "Clear All")
                    }
                    IconButton(onClick = { /* TODO: Toggle search visibility */ }) {
                        DSIcon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is ResourceState.Idle -> {
                    EmptyStateContent(
                        message = "Pull to refresh to load transactions",
                        actionText = "Load Transactions",
                        onAction = { onEvent(NetworkInspectorEvent.LoadTransactions) }
                    )
                }
                is ResourceState.Loading -> {
                    LoadingStateContent(message = "Loading transactions...")
                }
                is ResourceState.Success -> {
                    NetworkInspectorContent(
                        uiData = uiState.data,
                        onEvent = onEvent,
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
                is ResourceState.Error -> {
                    ErrorStateContent(
                        error = uiState.exception.message ?: "Unknown error",
                        message = uiState.message,
                        onRetry = { onEvent(NetworkInspectorEvent.LoadTransactions) },
                        onDismiss = { onEvent(NetworkInspectorEvent.ClearError) }
                    )
                }
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
}

@Composable
private fun NetworkInspectorContent(
    uiData: NetworkInspectorUiData,
    onEvent: (NetworkInspectorEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and Filters
        NetworkInspectorFilters(
            uiData = uiData,
            onEvent = onEvent
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transactions List
        if (uiData.transactions.isEmpty()) {
            EmptyStateContent(
                message = "No network transactions recorded yet",
                actionText = "Refresh",
                onAction = { onEvent(NetworkInspectorEvent.LoadTransactions) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiData.transactions) { transaction ->
                    NetworkTransactionItem(
                        transaction = transaction,
                        onClick = { onNavigateToDetail(transaction.id) }
                    )
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
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Method Filters
        DSText(
            text = "HTTP Methods",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            uiData.availableMethods.forEach { method ->
                FilterChip(
                    onClick = { 
                        val newMethod = if (uiData.selectedMethod == method) null else method
                        onEvent(NetworkInspectorEvent.MethodFilterChanged(newMethod))
                    },
                    label = { DSText(method) },
                    selected = uiData.selectedMethod == method,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Status Filters
        DSText(
            text = "Status",
            style = DSJarvisTheme.typography.body.medium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            uiData.availableStatuses.forEach { status ->
                FilterChip(
                    onClick = { 
                        val newStatus = if (uiData.selectedStatus == status) null else status
                        onEvent(NetworkInspectorEvent.StatusFilterChanged(newStatus))
                    },
                    label = { DSText(status) },
                    selected = uiData.selectedStatus == status,
                    modifier = Modifier.padding(end = 8.dp)
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
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
            
            Spacer(modifier = Modifier.height(4.dp))
            
            DSText(
                text = transaction.request.url,
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
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
    AlertDialog(
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
                style = DSButtonStyle.TEXT,
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun EmptyStateContent(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = message,
            style = DSJarvisTheme.typography.body.large,
            textAlign = TextAlign.Center,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        Spacer(modifier = Modifier.height(16.dp))
        DSButton(
            text = actionText,
            style = DSButtonStyle.OUTLINE,
            onClick = onAction
        )
    }
}

@Composable
private fun LoadingStateContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSCircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        DSText(
            text = message,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
    }
}

@Composable
private fun ErrorStateContent(
    error: String,
    message: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DSText(
            text = "Error loading network transactions",
            style = DSJarvisTheme.typography.body.large,
            fontWeight = FontWeight.Medium,
            color = DSJarvisTheme.colors.error.error100
        )
        Spacer(modifier = Modifier.height(8.dp))
        DSText(
            text = message ?: error,
            style = DSJarvisTheme.typography.body.medium,
            textAlign = TextAlign.Center,
            color = DSJarvisTheme.colors.neutral.neutral60
        )
        Spacer(modifier = Modifier.height(16.dp))
        DSButton(
            text = "Retry",
            style = DSButtonStyle.OUTLINE,
            onClick = onRetry
        )
    }
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
            onNavigateBack = {}
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
            onNavigateBack = {}
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
            onNavigateBack = {}
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
            onNavigateBack = {}
        )
    }
}