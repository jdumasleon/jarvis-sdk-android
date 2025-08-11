package com.jarvis.api

import android.app.Application
import com.jarvis.api.core.JarvisSDKProvider
import com.jarvis.config.JarvisConfig
import com.jarvis.config.JarvisConfigHolder

/**
 * Main Jarvis API entry point
 * Provides a clean, simple interface for SDK integration
 */
object Jarvis {
    
    /**
     * Initialize Jarvis SDK
     */
    fun initialize(
        application: Application,
        config: JarvisConfig = JarvisConfig()
    ) {
        // Store configuration globally for DI access
        JarvisConfigHolder.updateConfiguration(config)
        
        // Initialize through static method that will use DI
        JarvisSDKProvider.initialize(application, config)
    }
    
}