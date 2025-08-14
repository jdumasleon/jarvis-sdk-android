package com.jarvis.features.inspector.presentation.ui.transactionsDetails

import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.entity.HttpMethod
import com.jarvis.features.inspector.domain.entity.NetworkRequest
import com.jarvis.features.inspector.domain.entity.NetworkResponse
import com.jarvis.features.inspector.domain.entity.TransactionStatus

typealias NetworkTransactionDetailUiState = ResourceState<NetworkTransactionDetailUiData>

/**
 * UiData containing all screen state for NetworkTransactionDetailScreen
 */
data class NetworkTransactionDetailUiData(
    val transaction: NetworkTransaction,
    val selectedTab: Int = 0,
    val showDeleteConfirmation: Boolean = false,
    val availableTabs: List<String> = listOf("Overview", "Request", "Response")
) {
    companion object {
        val mockNetworkTransactionDetailUiData = NetworkTransactionDetailUiData(
            transaction = NetworkTransaction(
                id = "mock-transaction-1",
                request = NetworkRequest(
                    url = "https://api.example.com/users/123",
                    method = HttpMethod.GET,
                    headers = mapOf(
                        "Authorization" to "Bearer eyJhbGciOiJIUzI1NiIs...",
                        "Content-Type" to "application/json",
                        "User-Agent" to "Jarvis-SDK/1.0.0",
                        "Accept" to "application/json, text/plain, */*"
                    ),
                    body = null
                ),
                response = NetworkResponse(
                    statusCode = 200,
                    statusMessage = "OK",
                    headers = mapOf(
                        "Content-Type" to "application/json; charset=utf-8",
                        "Cache-Control" to "no-cache, private",
                        "Date" to "Wed, 21 Oct 2024 07:28:00 GMT",
                        "Server" to "nginx/1.18.0"
                    ),
                    body = """{
  "id": 123,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "avatar": "https://avatars.example.com/u/123",
  "created_at": "2024-01-15T10:30:00Z",
  "updated_at": "2024-10-21T07:25:30Z",
  "preferences": {
    "theme": "dark",
    "notifications": true,
    "language": "en"
  },
  "roles": ["user", "beta_tester"],
  "metadata": {
    "last_login": "2024-10-21T07:20:15Z",
    "login_count": 847,
    "subscription": "premium"
  }
}""",
                    contentType = "application/json"
                ),
                status = TransactionStatus.COMPLETED,
                startTime = System.currentTimeMillis() - 10000,
                endTime = System.currentTimeMillis() - 9658
            ),
            selectedTab = 0,
            showDeleteConfirmation = false
        )
        
        val mockErrorTransactionDetailUiData = NetworkTransactionDetailUiData(
            transaction = NetworkTransaction(
                id = "mock-transaction-error",
                request = NetworkRequest(
                    url = "https://api.example.com/users/999",
                    method = HttpMethod.POST,
                    headers = mapOf(
                        "Authorization" to "Bearer expired_token",
                        "Content-Type" to "application/json"
                    ),
                    body = """{
  "name": "New User",
  "email": "new@example.com",
  "password": "********"
}"""
                ),
                response = NetworkResponse(
                    statusCode = 401,
                    statusMessage = "Unauthorized",
                    headers = mapOf(
                        "Content-Type" to "application/json",
                        "WWW-Authenticate" to "Bearer realm=\"API\""
                    ),
                    body = """{
  "error": "unauthorized",
  "message": "Token has expired",
  "code": 401,
  "timestamp": "2024-10-21T07:28:00Z"
}""",
                    contentType = "application/json"
                ),
                status = TransactionStatus.COMPLETED,
                startTime = System.currentTimeMillis() - 8000,
                endTime = System.currentTimeMillis() - 7844
            ),
            selectedTab = 2
        )
    }
}

/**
 * Events for NetworkTransactionDetailScreen user interactions
 */
sealed interface NetworkTransactionDetailEvent {
    data class LoadTransaction(val transactionId: String) : NetworkTransactionDetailEvent
    data class SelectTab(val tabIndex: Int) : NetworkTransactionDetailEvent
    data class ShowDeleteConfirmation(val show: Boolean) : NetworkTransactionDetailEvent
    
    object DeleteTransaction : NetworkTransactionDetailEvent
    object ClearError : NetworkTransactionDetailEvent
}