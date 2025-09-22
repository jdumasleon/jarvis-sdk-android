@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.presentation.transactions

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.internal.domain.entity.HttpMethod
import com.jarvis.features.inspector.internal.domain.entity.NetworkRequest
import com.jarvis.features.inspector.internal.domain.entity.NetworkResponse
import com.jarvis.features.inspector.internal.domain.entity.TransactionStatus

typealias NetworkInspectorUiState = ResourceState<NetworkInspectorUiData>

/**
 * UiData containing all screen state for NetworkInspectorScreen
 * Excludes loading/error states which are handled by ResourceState
 */
data class NetworkInspectorUiData(
    val transactions: List<NetworkTransaction> = emptyList(), // Filtered transactions for display
    val allLoadedTransactions: List<NetworkTransaction> = emptyList(), // All transactions loaded so far
    val searchQuery: String = "",
    val selectedMethod: String? = null,
    val selectedStatus: String? = null,
    val availableMethods: List<String> = emptyList(),
    val availableStatuses: List<String> = emptyList(),
    val showClearConfirmation: Boolean = false,
    val selectedTransaction: NetworkTransaction? = null,
    val isRefreshing: Boolean = false,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = false,
    val isLoadingMore: Boolean = false
) {
    companion object {
        val mockNetworkInspectorUiData = NetworkInspectorUiData(
            transactions = listOf(
                NetworkTransaction(
                    id = "1",
                    request = NetworkRequest(
                        url = "https://api.example.com/users",
                        method = HttpMethod.GET,
                        headers = mapOf("Authorization" to "Bearer token123"),
                        body = null
                    ),
                    response = NetworkResponse(
                        statusCode = 200,
                        statusMessage = "OK",
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"users": [{"id": 1, "name": "John"}]}""",
                        contentType = "application/json"
                    ),
                    status = TransactionStatus.COMPLETED,
                    startTime = System.currentTimeMillis() - 5000,
                    endTime = System.currentTimeMillis() - 4755
                ),
                NetworkTransaction(
                    id = "2",
                    request = NetworkRequest(
                        url = "https://api.example.com/posts",
                        method = HttpMethod.POST,
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"title": "New Post", "content": "Post content"}"""
                    ),
                    response = NetworkResponse(
                        statusCode = 201,
                        statusMessage = "Created",
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"id": 123, "status": "created"}""",
                        contentType = "application/json"
                    ),
                    status = TransactionStatus.COMPLETED,
                    startTime = System.currentTimeMillis() - 3000,
                    endTime = System.currentTimeMillis() - 2844
                ),
                NetworkTransaction(
                    id = "3",
                    request = NetworkRequest(
                        url = "https://api.example.com/data",
                        method = HttpMethod.GET,
                        headers = mapOf(),
                        body = null
                    ),
                    response = null,
                    status = TransactionStatus.FAILED,
                    startTime = System.currentTimeMillis() - 1000,
                    endTime = null
                ),
                NetworkTransaction(
                    id = "4",
                    request = NetworkRequest(
                        url = "https://api.example.com/users",
                        method = HttpMethod.GET,
                        headers = mapOf("Authorization" to "Bearer token123"),
                        body = null
                    ),
                    response = NetworkResponse(
                        statusCode = 200,
                        statusMessage = "OK",
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"users": [{"id": 1, "name": "John"}]}""",
                        contentType = "application/json"
                    ),
                    status = TransactionStatus.PENDING,
                    startTime = System.currentTimeMillis() - 5000,
                    endTime = null
                ),
                NetworkTransaction(
                    id = "5",
                    request = NetworkRequest(
                        url = "https://api.example.com/",
                        method = HttpMethod.POST,
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"title": "New Post", "content": "Post content"}"""
                    ),
                    response = NetworkResponse(
                        statusCode = 20,
                        statusMessage = "Created",
                        headers = mapOf("Content-Type" to "application/json"),
                        body = """{"id": 123, "status": "created"}""",
                        contentType = "application/json"
                    ),
                    status = TransactionStatus.COMPLETED,
                    startTime = System.currentTimeMillis() - 3000,
                    endTime = System.currentTimeMillis() - 2844
                )
            ),
            searchQuery = "api.example.com",
            selectedMethod = "GET",
            availableMethods = listOf("GET", "PATCH", "POST", "PUT", "DELETE"),
            availableStatuses = listOf("COMPLETED", "FAILED", "PENDING"),
            showClearConfirmation = false,
            selectedTransaction = null
        )
    }
}

/**
 * Events for NetworkInspectorScreen user interactions
 */
sealed interface NetworkInspectorEvent {
    data class SearchQueryChanged(val query: String) : NetworkInspectorEvent
    data class MethodFilterChanged(val method: String?) : NetworkInspectorEvent
    data class StatusFilterChanged(val status: String?) : NetworkInspectorEvent
    data class TransactionSelected(val transaction: NetworkTransaction) : NetworkInspectorEvent
    data class DeleteTransaction(val transactionId: String) : NetworkInspectorEvent
    data class ShowClearConfirmation(val show: Boolean) : NetworkInspectorEvent
    
    object LoadTransactions : NetworkInspectorEvent
    object LoadMoreTransactions : NetworkInspectorEvent
    object ClearAllTransactions : NetworkInspectorEvent
    object RefreshTransactions : NetworkInspectorEvent
    object ClearError : NetworkInspectorEvent
}