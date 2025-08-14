package com.jarvis.api.core

import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.core.data.performance.PerformanceManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing Jarvis SDK dependencies through Hilt
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface JarvisSDKEntryPoint {
    
    /**
     * Get ConfigurationSynchronizer instance
     */
    fun getConfigurationSynchronizer(): ConfigurationSynchronizer
    
    /**
     * Get PerformanceManager instance
     */
    fun getPerformanceManager(): PerformanceManager
}