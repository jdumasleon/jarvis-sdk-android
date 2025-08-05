package com.jarvis.features.inspector.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.presentation.components.ConfirmationDialog
import com.jarvis.core.presentation.components.ErrorContent
import com.jarvis.core.presentation.components.LoadingContent
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkTransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NetworkTransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {
        viewModel.onEvent(NetworkTransactionDetailEvent.LoadTransaction(transactionId))
    }
    
    NetworkTransactionDetailScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onDeleted = onDeleted
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkTransactionDetailScreen(
    uiState: NetworkTransactionDetailUiState,
    onEvent: (NetworkTransactionDetailEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.isSuccess) {
                        IconButton(
                            onClick = {
                                onEvent(NetworkTransactionDetailEvent.ShowDeleteConfirmation(true))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        ResourceStateContent(
            resourceState = uiState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRetry = { /* Retry can be handled by reloading */ },
            onDismiss = { onEvent(NetworkTransactionDetailEvent.ClearError) },
            loadingMessage = "Loading transaction details...",
            emptyMessage = "Transaction not found"
        ) { uiData ->
            TransactionDetailContent(
                uiData = uiData,
                onEvent = onEvent
            )
        }
        
        // Delete Confirmation Dialog
        uiState.getDataOrNull()?.let { data ->
            if (data.showDeleteConfirmation) {
                ConfirmationDialog(
                    title = "Delete Transaction",
                    message = "Are you sure you want to delete this transaction? This action cannot be undone.",
                    confirmText = "Delete",
                    onConfirm = {
                        onEvent(NetworkTransactionDetailEvent.DeleteTransaction)
                        onEvent(NetworkTransactionDetailEvent.ShowDeleteConfirmation(false))
                        onDeleted()
                    },
                    onDismiss = {
                        onEvent(NetworkTransactionDetailEvent.ShowDeleteConfirmation(false))
                    }
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    uiData: NetworkTransactionDetailUiData,
    onEvent: (NetworkTransactionDetailEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = uiData.selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            uiData.availableTabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiData.selectedTab == index,
                    onClick = { onEvent(NetworkTransactionDetailEvent.SelectTab(index)) },
                    text = { Text(title) }
                )
            }
        }

        // Tab Content
        when (uiData.selectedTab) {
            0 -> TransactionOverviewTab(
                transaction = uiData.transaction,
                modifier = Modifier.weight(1f)
            )
            1 -> TransactionRequestTab(
                request = uiData.transaction.request,
                modifier = Modifier.weight(1f)
            )
            2 -> TransactionResponseTab(
                response = uiData.transaction.response,
                modifier = Modifier.weight(1f)
            )
            3 -> TransactionBodyTab(
                transaction = uiData.transaction,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Preview Templates
@Preview(showBackground = true, name = "Transaction Detail - Success")
@Composable
fun NetworkTransactionDetailScreenSuccessPreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Success(NetworkTransactionDetailUiData.mockNetworkTransactionDetailUiData),
            onEvent = { },
            onNavigateBack = { },
            onDeleted = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Error Response")
@Composable
fun NetworkTransactionDetailScreenErrorPreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Success(NetworkTransactionDetailUiData.mockErrorTransactionDetailUiData),
            onEvent = { },
            onNavigateBack = { },
            onDeleted = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Loading")
@Composable
fun NetworkTransactionDetailScreenLoadingPreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Loading,
            onEvent = { },
            onNavigateBack = { },
            onDeleted = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Error")
@Composable
fun NetworkTransactionDetailScreenErrorStatePreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Error(
                RuntimeException("Transaction not found"),
                "The requested transaction could not be found"
            ),
            onEvent = { },
            onNavigateBack = { },
            onDeleted = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Delete Confirmation")
@Composable
fun NetworkTransactionDetailScreenDeleteConfirmationPreview() {
    MaterialTheme {
        val uiData = NetworkTransactionDetailUiData.mockNetworkTransactionDetailUiData.copy(
            showDeleteConfirmation = true
        )
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Success(uiData),
            onEvent = { },
            onNavigateBack = { },
            onDeleted = { }
        )
    }
}