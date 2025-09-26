@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.domain.usecase

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.repository.NetworkRepository
import javax.inject.Inject

internal class ClearNetworkTransactionsUseCase @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    suspend operator fun invoke() {
        networkRepository.deleteAllTransactions()
    }
}