package com.jarvis.features.inspector.presentation.ui

import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.HttpMethod
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.TransactionStatus

typealias NetworkInspectorUiState = ResourceState<NetworkInspectorUiData>

/**
 * UiData containing all screen state for NetworkInspectorScreen
 * Excludes loading/error states which are handled by ResourceState
 */
data class NetworkInspectorUiData(
    val transactions: List<NetworkTransaction> = emptyList(),
    val searchQuery: String = "",
    val selectedMethod: String? = null,
    val selectedStatus: String? = null,
    val availableMethods: List<String> = emptyList(),
    val availableStatuses: List<String> = emptyList(),
    val showClearConfirmation: Boolean = false,
    val selectedTransaction: NetworkTransaction? = null
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
                )
            ),
            searchQuery = "api.example.com",
            selectedMethod = "GET",
            availableMethods = listOf("GET", "POST", "PUT", "DELETE"),
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
    object ClearAllTransactions : NetworkInspectorEvent
    object RefreshTransactions : NetworkInspectorEvent
    object ClearError : NetworkInspectorEvent
}