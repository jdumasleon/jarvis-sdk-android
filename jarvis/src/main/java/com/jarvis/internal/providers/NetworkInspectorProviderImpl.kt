package com.jarvis.internal.providers

import com.jarvis.api.providers.NetworkInspectorProvider
import com.jarvis.config.JarvisConfig
import com.jarvis.features.inspector.lib.api.JarvisNetworkInspector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Internal implementation of NetworkInspectorProvider
 * 
 * This is the actual implementation that uses the JarvisNetworkInspector
 * to provide network inspection capabilities. It's internal and should not be used directly by host applications.
 */
@Singleton
class NetworkInspectorProviderImpl @Inject constructor(
    private val jarvisConfig: JarvisConfig,
    private val jarvisNetworkInspector: JarvisNetworkInspector
) : NetworkInspectorProvider {
    
    override fun createInterceptor(): Interceptor? {
        if (!isEnabled()) {
            return null
        }
        
        return jarvisNetworkInspector.createInterceptor()
    }
    
    override fun clearAllTransactions() {
        if (!isEnabled()) return
        
        jarvisNetworkInspector.clearTransactions()
    }
    
    override fun clearOldTransactions(beforeTimestamp: Long) {
        if (!isEnabled()) return
        
        jarvisNetworkInspector.clearOldTransactions(beforeTimestamp)
    }
    
    override fun getTransactionCount(): Flow<Int> {
        if (!isEnabled()) {
            return flow { emit(0) }
        }
        
        return flow {
            emit(runBlocking { jarvisNetworkInspector.getTransactionCount() })
        }
    }
    
    override fun isEnabled(): Boolean {
        return jarvisConfig.networkInspection.enableNetworkLogging
    }
}