package com.jarvis.features.inspector.domain.usecase

import com.jarvis.features.inspector.domain.repository.NetworkRepository
import javax.inject.Inject

class ClearNetworkTransactionsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke() {
        networkRepository.deleteAllTransactions()
    }
}