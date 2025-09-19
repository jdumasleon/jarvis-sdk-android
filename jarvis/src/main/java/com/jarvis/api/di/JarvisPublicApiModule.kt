package com.jarvis.api.di

import com.jarvis.api.providers.NetworkInspectorProvider
import com.jarvis.config.JarvisConfig
import com.jarvis.config.JarvisConfigHolder
import com.jarvis.internal.providers.NetworkInspectorProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * JarvisPublicApiModule - Clean DI exposition for public service interfaces
 * 
 * This module provides the binding between public service interfaces and their
 * internal implementations. This is the only module that host applications need
 * to be aware of for dependency injection.
 * 
 * Features:
 * - Clean separation between public API and internal implementation
 * - Singleton scoped providers for performance
 * - Easy testing via interface mocking
 * - Graceful degradation when services are unavailable
 * 
 * Usage in host applications:
 * ```kotlin
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object NetworkModule {
 *     
 *     @Provides
 *     @Singleton
 *     fun provideOkHttpClient(
 *         loggingInterceptor: HttpLoggingInterceptor,
 *         networkInspector: NetworkInspectorProvider // Injected automatically
 *     ): OkHttpClient {
 *         return OkHttpClient.Builder()
 *             .addInterceptor(loggingInterceptor)
 *             .apply {
 *                 networkInspector.createInterceptor()?.let { interceptor ->
 *                     addInterceptor(interceptor)
 *                 }
 *             }
 *             .build()
 *     }
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class JarvisPublicApiModule {
    
    /**
     * Binds the internal NetworkInspectorProvider implementation to the public interface
     * 
     * This binding allows host applications to inject NetworkInspectorProvider
     * and receive the fully functional implementation with all network inspection
     * capabilities.
     */
    @Binds
    @Singleton
    abstract fun bindNetworkInspectorProvider(
        implementation: NetworkInspectorProviderImpl
    ): NetworkInspectorProvider
    
    companion object {
        /**
         * Provides the JarvisConfig instance for dependency injection
         * 
         * This method retrieves the configuration from JarvisConfigHolder which is
         * updated when the SDK is initialized. This bridges the gap between
         * static configuration and dependency injection.
         */
        @Provides
        @Singleton
        fun provideJarvisConfig(): JarvisConfig {
            return JarvisConfigHolder.getConfiguration()
        }
    }
}