package com.jarvis.demo.presentation.inspector

import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.data.repository.ApiCallResult

typealias InspectorUiState = ResourceState<InspectorUiData>

/**
 * UiData containing all screen state for InspectorScreen
 */
data class InspectorUiData(
    val apiCalls: List<ApiCallResult> = emptyList(),
    val isPerformingCalls: Boolean = false,
    val totalCallsPerformed: Int = 0,
    val successfulCalls: Int = 0,
    val failedCalls: Int = 0,
    val showClearConfirmation: Boolean = false,
    val selectedCall: ApiCallResult? = null
) {
    val successRate: Float
        get() = if (totalCallsPerformed > 0) successfulCalls.toFloat() / totalCallsPerformed else 0f
        
    companion object {
        val mockInspectorUiData = InspectorUiData(
            apiCalls = listOf(
                ApiCallResult(
                    url = "https://jsonplaceholder.typicode.com/posts/1",
                    host = "jsonplaceholder.typicode.com",
                    method = "GET",
                    startTime = System.currentTimeMillis() - 10000,
                    endTime = System.currentTimeMillis() - 10000 + 245,
                    isSuccess = true,
                    statusCode = 200,
                    error = null
                ),
                ApiCallResult(
                    url = "https://httpbin.org/post",
                    host = "httpbin.org",
                    method = "POST",
                    startTime = System.currentTimeMillis() - 8000,
                    endTime = System.currentTimeMillis() - 8000 + 456,
                    isSuccess = true,
                    statusCode = 200,
                    error = null
                ),
                ApiCallResult(
                    url = "https://httpbin.org/status/404",
                    host = "httpbin.org",
                    method = "GET",
                    startTime = System.currentTimeMillis() - 6000,
                    endTime = System.currentTimeMillis() - 6000 + 123,
                    isSuccess = false,
                    statusCode = 404,
                    error = "Not Found"
                ),
                ApiCallResult(
                    url = "https://httpbin.org/delay/10",
                    host = "httpbin.org",
                    method = "GET",
                    startTime = System.currentTimeMillis() - 4000,
                    endTime = System.currentTimeMillis() - 4000,
                    isSuccess = false,
                    statusCode = 0,
                    error = "Request timeout"
                ),
                ApiCallResult(
                    url = "https://api.github.com/users/octocat",
                    host = "api.github.com",
                    method = "GET",
                    startTime = System.currentTimeMillis() - 2000,
                    endTime = System.currentTimeMillis() - 2000 + 189,
                    isSuccess = true,
                    statusCode = 200,
                    error = null
                )
            ).sortedByDescending { it.startTime },
            isPerformingCalls = false,
            totalCallsPerformed = 5,
            successfulCalls = 3,
            failedCalls = 2
        )
        
        val mockInspectorPerformingUiData = mockInspectorUiData.copy(
            isPerformingCalls = true
        )
    }
}

/**
 * Events for InspectorScreen user interactions
 */
sealed interface InspectorEvent {
    data class CallSelected(val call: ApiCallResult) : InspectorEvent
    data class ShowClearConfirmation(val show: Boolean) : InspectorEvent
    
    object PerformInitialApiCalls : InspectorEvent
    object AddRandomApiCall : InspectorEvent
    object ClearApiCalls : InspectorEvent
    object RefreshCalls : InspectorEvent
    object ClearError : InspectorEvent
}