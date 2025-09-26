package com.jarvis.demo.domain.usecase.home

import com.jarvis.demo.data.repository.DemoHomeRepository
import retrofit2.Response
import javax.inject.Inject

class RefreshDataUseCase @Inject constructor(
    private val demoHomeRepository: DemoHomeRepository
) {

    suspend fun execute(): Triple<Response<*>?, Response<*>?, Response<*>?> {
        return demoHomeRepository.refreshData()
    }
}