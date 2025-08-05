package com.jarvis.api

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import com.jarvis.api.core.JarvisSDK
import com.jarvis.api.core.JarvisConfiguration
import com.jarvis.api.core.JarvisProvider

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
        config: JarvisConfiguration = JarvisConfiguration.development()
    ) {
        val context = application.applicationContext
        val sdk = JarvisSDK(context)
        sdk.initialize(application, config)
    }
    
    /**
     * Composable wrapper for easy integration
     */
    @Composable
    fun WithJarvis(
        sdk: JarvisSDK,
        content: @Composable () -> Unit
    ) {
        JarvisProvider(sdk = sdk) {
            content()
        }
    }
}