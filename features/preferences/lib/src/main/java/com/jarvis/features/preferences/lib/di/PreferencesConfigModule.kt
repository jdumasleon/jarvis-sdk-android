package com.jarvis.features.preferences.lib.di

import com.jarvis.features.preferences.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.domain.config.PreferencesConfigProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI Module for preferences configuration (Lib layer)
 * Binds domain interface to data implementation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesConfigModule {
    
    @Binds
    @Singleton
    abstract fun bindPreferencesConfigProvider(
        impl: PreferencesConfigProviderImpl
    ): PreferencesConfigProvider
}