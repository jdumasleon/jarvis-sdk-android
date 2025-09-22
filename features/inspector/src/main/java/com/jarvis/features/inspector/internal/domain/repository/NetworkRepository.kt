package com.jarvis.features.inspector.internal.domain.repository

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface NetworkRepository {
    
    fun getAllTransactions(): Flow<List<NetworkTransaction>>

    fun getTransactionsPaged(limit: Int = 20, offset: Int = 0): Flow<List<NetworkTransaction>>
    
    fun getTransaction(id: String): Flow<NetworkTransaction?>
    
    fun searchTransactions(query: String): Flow<List<NetworkTransaction>>
    
    fun getTransactionsByMethod(method: String): Flow<List<NetworkTransaction>>
    
    fun getTransactionsByStatus(status: String): Flow<List<NetworkTransaction>>
    
    suspend fun insertTransaction(transaction: NetworkTransaction)
    
    suspend fun updateTransaction(transaction: NetworkTransaction)
    
    suspend fun deleteTransaction(id: String)
    
    suspend fun deleteAllTransactions()
    
    suspend fun getTransactionCount(): Int
    
    suspend fun deleteOldTransactions(beforeTimestamp: Long)
}