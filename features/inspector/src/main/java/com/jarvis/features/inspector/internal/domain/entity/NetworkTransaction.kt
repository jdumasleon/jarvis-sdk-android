@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.inspector.internal.domain.entity

import androidx.annotation.RestrictTo

import java.util.UUID

data class NetworkTransaction(
    val id: String = UUID.randomUUID().toString(),
    val request: NetworkRequest,
    val response: NetworkResponse? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val error: String? = null
) {
    val duration: Long?
        get() = if (endTime != null) endTime - startTime else null
        
    val isCompleted: Boolean
        get() = status == TransactionStatus.COMPLETED
        
    val isFailed: Boolean
        get() = status == TransactionStatus.FAILED
        
    val isPending: Boolean
        get() = status == TransactionStatus.PENDING
        
    fun withResponse(response: NetworkResponse): NetworkTransaction {
        return copy(
            response = response,
            endTime = System.currentTimeMillis(),
            status = TransactionStatus.COMPLETED
        )
    }
    
    fun withError(error: String): NetworkTransaction {
        return copy(
            error = error,
            endTime = System.currentTimeMillis(),
            status = TransactionStatus.FAILED
        )
    }
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}