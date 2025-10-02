package com.jarvis.demo.koin.domain.usecase.inspector

import com.jarvis.demo.koin.data.repository.ApiCallResult
import com.jarvis.demo.koin.data.repository.DemoApiRepository

class PerformApiCallsUseCase (
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