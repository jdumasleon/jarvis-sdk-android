package com.jarvis.features.inspector.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jarvis.features.inspector.data.local.entity.NetworkTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkTransactionDao {
    
    @Query("SELECT * FROM network_transactions ORDER BY start_time DESC LIMIT 1000")
    fun getAllTransactions(): Flow<List<NetworkTransactionEntity>>
    
    // ✅ PAGINATION: Add paginated query for better performance with large datasets
    @Query("SELECT * FROM network_transactions ORDER BY start_time DESC LIMIT :limit OFFSET :offset")
    fun getTransactionsPaged(limit: Int, offset: Int): Flow<List<NetworkTransactionEntity>>
    
    // ✅ PERFORMANCE: Get recent transactions only (last hour by default)
    @Query("SELECT * FROM network_transactions WHERE start_time >= :afterTimestamp ORDER BY start_time DESC LIMIT :limit")
    fun getRecentTransactions(afterTimestamp: Long = System.currentTimeMillis() - 3600000L, limit: Int = 100): Flow<List<NetworkTransactionEntity>>
    
    @Query("SELECT * FROM network_transactions WHERE id = :id")
    fun getTransaction(id: String): Flow<NetworkTransactionEntity?>
    
    @Query("""
        SELECT * FROM network_transactions 
        WHERE url LIKE '%' || :query || '%' 
        OR method LIKE '%' || :query || '%'
        OR response_status_code LIKE '%' || :query || '%'
        ORDER BY start_time DESC
    """)
    fun searchTransactions(query: String): Flow<List<NetworkTransactionEntity>>
    
    @Query("""
        SELECT * FROM network_transactions 
        WHERE method = :method 
        ORDER BY start_time DESC
    """)
    fun getTransactionsByMethod(method: String): Flow<List<NetworkTransactionEntity>>
    
    @Query("""
        SELECT * FROM network_transactions 
        WHERE status = :status 
        ORDER BY start_time DESC
    """)
    fun getTransactionsByStatus(status: String): Flow<List<NetworkTransactionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: NetworkTransactionEntity)
    
    @Update
    suspend fun updateTransaction(transaction: NetworkTransactionEntity)
    
    @Query("DELETE FROM network_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
    
    @Query("DELETE FROM network_transactions")
    suspend fun deleteAllTransactions()
    
    @Query("SELECT COUNT(*) FROM network_transactions")
    suspend fun getTransactionCount(): Int
    
    @Query("DELETE FROM network_transactions WHERE start_time < :beforeTimestamp")
    suspend fun deleteOldTransactions(beforeTimestamp: Long)
    
    @Query("""
        SELECT * FROM network_transactions 
        WHERE start_time >= :startTime AND start_time <= :endTime 
        ORDER BY start_time DESC
    """)
    fun getTransactionsByTimeRange(startTime: Long, endTime: Long): Flow<List<NetworkTransactionEntity>>
    
    @Query("""
        SELECT DISTINCT method FROM network_transactions 
        ORDER BY method ASC
    """)
    suspend fun getDistinctMethods(): List<String>
    
    @Query("""
        SELECT DISTINCT status FROM network_transactions 
        ORDER BY status ASC
    """)
    suspend fun getDistinctStatuses(): List<String>
}