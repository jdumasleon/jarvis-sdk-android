package com.jarvis.features.inspector.domain.usecase

import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllNetworkTransactionsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Flow<List<NetworkTransaction>> {
        return networkRepository.getAllTransactions()
    }
}