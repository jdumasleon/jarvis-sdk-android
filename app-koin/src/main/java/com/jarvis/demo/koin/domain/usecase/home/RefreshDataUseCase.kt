package com.jarvis.demo.koin.domain.usecase.home

import com.jarvis.demo.koin.data.repository.DemoHomeRepository
import retrofit2.Response

class RefreshDataUseCase (
    private val demoHomeRepository: DemoHomeRepository
) {

    suspend fun execute(): Triple<Response<*>?, Response<*>?, Response<*>?> {
        return demoHomeRepository.refreshData()
    }
}