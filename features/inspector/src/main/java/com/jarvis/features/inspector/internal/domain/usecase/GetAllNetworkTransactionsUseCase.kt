@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.internal.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class GetAllNetworkTransactionsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Flow<List<NetworkTransaction>> {
        return networkRepository.getAllTransactions()
    }
}