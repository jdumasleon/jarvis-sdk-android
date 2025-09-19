package com.jarvis.noop

import com.jarvis.api.providers.NetworkInspectorProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * No-op implementation of NetworkInspectorProvider
 * 
 * This implementation provides graceful degradation when network inspection
 * is disabled or unavailable. All methods return safe default values
 * and perform no actual operations.
 * 
 * Used in scenarios:
 * - Release builds where inspection should be disabled
 * - When network inspection features are explicitly disabled in configuration
 * - Error recovery when the full implementation fails to initialize
 */
@Singleton
class NoOpNetworkInspectorProvider @Inject constructor() : NetworkInspectorProvider {
    
    override fun createInterceptor(): Interceptor? {
        // Return null to indicate no interceptor should be added
        return null
    }
    
    override fun clearAllTransactions() {
        // No-op: nothing to clear
    }
    
    override fun clearOldTransactions(beforeTimestamp: Long) {
        // No-op: nothing to clear
    }
    
    override fun getTransactionCount(): Flow<Int> {
        // Always return 0 transactions
        return flowOf(0)
    }
    
    override fun isEnabled(): Boolean {
        // Always return false to indicate inspection is not available
        return false
    }
}