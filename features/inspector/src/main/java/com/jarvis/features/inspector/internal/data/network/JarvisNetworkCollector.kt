package com.jarvis.features.inspector.internal.data.network

import androidx.annotation.RestrictTo

import com.jarvis.features.inspector.internal.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.internal.domain.repository.NetworkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class JarvisNetworkCollector @Inject constructor(
    private val networkRepository: NetworkRepository
) {
    
    private val collectorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    suspend fun onRequestSent(transaction: NetworkTransaction) {
        try {
            networkRepository.insertTransaction(transaction)
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("JarvisNetworkCollector: Error storing request - ${e.message}")
        }
    }
    
    suspend fun onResponseReceived(transaction: NetworkTransaction) {
        try {
            networkRepository.updateTransaction(transaction)
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("JarvisNetworkCollector: Error updating transaction with response - ${e.message}")
        }
    }
    
    suspend fun onFailure(transaction: NetworkTransaction, error: Throwable) {
        try {
            networkRepository.updateTransaction(transaction)
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("JarvisNetworkCollector: Error updating transaction with failure - ${e.message}")
        }
    }
    
    fun clearAll() {
        collectorScope.launch {
            try {
                networkRepository.deleteAllTransactions()
            } catch (e: Exception) {
                println("JarvisNetworkCollector: Error clearing transactions - ${e.message}")
            }
        }
    }
    
    fun clearOldTransactions(beforeTimestamp: Long) {
        collectorScope.launch {
            try {
                networkRepository.deleteOldTransactions(beforeTimestamp)
            } catch (e: Exception) {
                println("JarvisNetworkCollector: Error clearing old transactions - ${e.message}")
            }
        }
    }
    
    suspend fun getTransactionCount(): Int {
        return try {
            networkRepository.getTransactionCount()
        } catch (e: Exception) {
            println("JarvisNetworkCollector: Error getting transaction count - ${e.message}")
            0
        }
    }
}