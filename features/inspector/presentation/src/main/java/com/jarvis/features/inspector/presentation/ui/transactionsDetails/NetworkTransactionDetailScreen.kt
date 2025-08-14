package com.jarvis.features.inspector.presentation.ui.transactionsDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jarvis.core.designsystem.component.DSTabBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.components.ConfirmationDialog
import com.jarvis.core.presentation.components.ResourceStateContent
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.presentation.ui.components.TransactionOverviewTab
import com.jarvis.features.inspector.presentation.ui.components.TransactionRequestTab
import com.jarvis.features.inspector.presentation.ui.components.TransactionResponseTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkTransactionDetailRoute(
    transactionId: String,
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
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkTransactionDetailScreen(
    uiState: NetworkTransactionDetailUiState,
    onEvent: (NetworkTransactionDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .padding(top = DSJarvisTheme.spacing.m)
            .fillMaxSize()
    ) {
        ResourceStateContent(
            resourceState = uiState,
            modifier = modifier.fillMaxSize(),
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
                },
                onDismiss = {
                    onEvent(NetworkTransactionDetailEvent.ShowDeleteConfirmation(false))
                }
            )
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

        // Sections Tabs
        TransactionsDetailSectionsTabs(
            selectedTab = uiData.selectedTab,
            onTabSelected = { onEvent(NetworkTransactionDetailEvent.SelectTab(it)) }
        )

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
        }
    }
}

@Composable
private fun TransactionsDetailSectionsTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabLabels = listOf("Overview", "Request", "Response")

    DSTabBar(
        selectedTabIndex = selectedTab,
        tabCount = tabLabels.size,
        onTabSelected = { index -> onTabSelected(index) },
        backgroundColor = DSJarvisTheme.colors.extra.white
    ) { index, selected ->
        Box (
            modifier = Modifier.padding(DSJarvisTheme.spacing.m)
        ) {
            DSText(
                text = tabLabels[index],
                style = DSJarvisTheme.typography.body.medium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    DSJarvisTheme.colors.primary.primary60
                } else {
                    DSJarvisTheme.colors.neutral.neutral100
                }
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
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Error Response")
@Composable
fun NetworkTransactionDetailScreenErrorPreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Success(NetworkTransactionDetailUiData.mockErrorTransactionDetailUiData),
            onEvent = { }
        )
    }
}

@Preview(showBackground = true, name = "Transaction Detail - Loading")
@Composable
fun NetworkTransactionDetailScreenLoadingPreview() {
    MaterialTheme {
        NetworkTransactionDetailScreen(
            uiState = ResourceState.Loading,
            onEvent = { }
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
            onEvent = { }
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
            onEvent = { }
        )
    }
}