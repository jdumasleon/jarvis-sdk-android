package com.jarvis.demo.domain.usecase.inspector

import com.jarvis.demo.data.repository.ApiCallResult
import com.jarvis.demo.data.repository.DemoApiRepository
import javax.inject.Inject

class PerformApiCallsUseCase @Inject constructor(
    private val repository: DemoApiRepository
) {

    suspend fun performRandomApiCall(): ApiCallResult {
        return repository.performRandomApiCall()
    }

    suspend fun performInitialApiCalls(numberOfCalls: Int = (10..20).random()): List<ApiCallResult> {
        val results = mutableListOf<ApiCallResult>()
        repeat(numberOfCalls) {
            results.add(repository.performRandomApiCall())
        }
        return results.sortedByDescending { it.startTime }
    }

    suspend fun performRefreshApiCalls(numberOfCalls: Int = 3): List<ApiCallResult> {
        val results = mutableListOf<ApiCallResult>()
        repeat(numberOfCalls) {
            results.add(repository.performRandomApiCall())
        }
        return results.sortedByDescending { it.startTime }
    }
}