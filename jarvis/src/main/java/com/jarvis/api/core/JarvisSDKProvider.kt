package com.jarvis.api.core

import android.app.Application
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.config.JarvisConfig
import dagger.hilt.android.EntryPointAccessors

/**
 * Provider for JarvisSDK that bridges static initialization with dependency injection
 */
object JarvisSDKProvider {
    
    @Volatile
    private var sdkInstance: JarvisSDK? = null
    
    /**
     * Initialize the JarvisSDK using Hilt dependency injection
     */
    fun initialize(application: Application, config: JarvisConfig) {
        if (sdkInstance != null) {
            return
        }
        
        // Get dependencies through Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            application,
            JarvisSDKEntryPoint::class.java
        )
        
        val configurationSynchronizer = entryPoint.getConfigurationSynchronizer()
        val performanceManager = entryPoint.getPerformanceManager()
        
        // Create SDK instance manually with injected dependencies
        sdkInstance = JarvisSDK(application.applicationContext, configurationSynchronizer, performanceManager)
        sdkInstance?.initialize(application, config)
    }
    
    /**
     * Get the initialized SDK instance
     */
    fun getInstance(): JarvisSDK? {
        return sdkInstance
    }
}