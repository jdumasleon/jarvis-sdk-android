package com.jarvis.features.inspector.data.repository

import com.jarvis.features.inspector.data.local.dao.NetworkTransactionDao
import com.jarvis.features.inspector.data.mapper.NetworkTransactionMapper
import com.jarvis.features.inspector.domain.entity.NetworkTransaction
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkRepositoryImpl @Inject constructor(
    private val dao: NetworkTransactionDao,
    private val mapper: NetworkTransactionMapper
) : NetworkRepository {
    
    override fun getAllTransactions(): Flow<List<NetworkTransaction>> {
        return dao.getAllTransactions().map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    // ✅ PAGINATION: Implement paginated transactions for better performance
    override fun getTransactionsPaged(limit: Int, offset: Int): Flow<List<NetworkTransaction>> {
        return dao.getTransactionsPaged(limit, offset).map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    override fun getTransaction(id: String): Flow<NetworkTransaction?> {
        return dao.getTransaction(id).map { entity ->
            entity?.let { mapper.toDomain(it) }
        }
    }
    
    override fun searchTransactions(query: String): Flow<List<NetworkTransaction>> {
        return dao.searchTransactions(query).map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    override fun getTransactionsByMethod(method: String): Flow<List<NetworkTransaction>> {
        return dao.getTransactionsByMethod(method).map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    override fun getTransactionsByStatus(status: String): Flow<List<NetworkTransaction>> {
        return dao.getTransactionsByStatus(status).map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
    
    override suspend fun insertTransaction(transaction: NetworkTransaction) {
        dao.insertTransaction(mapper.toEntity(transaction))
    }
    
    override suspend fun updateTransaction(transaction: NetworkTransaction) {
        dao.updateTransaction(mapper.toEntity(transaction))
    }
    
    override suspend fun deleteTransaction(id: String) {
        dao.deleteTransaction(id)
    }
    
    override suspend fun deleteAllTransactions() {
        dao.deleteAllTransactions()
    }
    
    override suspend fun getTransactionCount(): Int {
        return dao.getTransactionCount()
    }
    
    override suspend fun deleteOldTransactions(beforeTimestamp: Long) {
        dao.deleteOldTransactions(beforeTimestamp)
    }
}