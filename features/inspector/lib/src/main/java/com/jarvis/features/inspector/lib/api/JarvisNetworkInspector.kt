package com.jarvis.features.inspector.lib.api

import android.content.Context
import com.jarvis.features.inspector.data.network.JarvisNetworkCollector
import com.jarvis.features.inspector.data.network.JarvisNetworkInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JarvisNetworkInspector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkCollector: JarvisNetworkCollector
) {
    
    fun createInterceptor(): Interceptor {
        return JarvisNetworkInterceptor.Builder()
            .collector(networkCollector)
            .build()
    }
    
    fun clearTransactions() {
        networkCollector.clearAll()
    }
    
    fun clearOldTransactions(beforeTimestamp: Long) {
        networkCollector.clearOldTransactions(beforeTimestamp)
    }
    
    suspend fun getTransactionCount(): Int {
        return networkCollector.getTransactionCount()
    }
    
    companion object {
        
        @JvmStatic
        fun createInterceptor(context: Context): Interceptor {
            // This is for external usage when Hilt is not available
            throw IllegalStateException(
                "Please use JarvisNetworkInspector instance from Hilt dependency injection. " +
                "Inject JarvisNetworkInspector and call createInterceptor() method."
            )
        }
    }
}