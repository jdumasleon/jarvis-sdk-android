package com.jarvis.api.providers

import kotlinx.coroutines.flow.Flow
import okhttp3.Interceptor

/**
 * NetworkInspectorProvider - Clean interface for network inspection capabilities
 * 
 * This interface provides a clean abstraction for network monitoring functionality,
 * allowing host applications to integrate network inspection without coupling to
 * internal implementation details.
 * 
 * Features:
 * - HTTP/HTTPS request/response monitoring
 * - Transaction management and cleanup
 * - OkHttp interceptor creation
 * - Conditional availability based on configuration
 * 
 * Example usage in host applications:
 * ```kotlin
 * @Inject
 * lateinit var networkInspector: NetworkInspectorProvider
 * 
 * val client = OkHttpClient.Builder()
 *     .apply {
 *         networkInspector.createInterceptor()?.let { interceptor ->
 *             addInterceptor(interceptor)
 *         }
 *     }
 *     .build()
 * ```
 */
interface NetworkInspectorProvider {
    
    /**
     * Create an OkHttp interceptor for network monitoring
     * 
     * @return Interceptor instance that can be added to OkHttpClient, or null if not available
     */
    fun createInterceptor(): Interceptor?
    
    /**
     * Clear all recorded network transactions
     */
    fun clearAllTransactions()
    
    /**
     * Clear transactions older than the specified timestamp
     * 
     * @param beforeTimestamp Timestamp in milliseconds
     */
    fun clearOldTransactions(beforeTimestamp: Long)
    
    /**
     * Get the total number of recorded transactions
     * 
     * @return Flow emitting the current transaction count
     */
    fun getTransactionCount(): Flow<Int>
    
    /**
     * Check if network inspection is currently enabled
     * 
     * @return true if network inspection is active, false otherwise
     */
    fun isEnabled(): Boolean
}