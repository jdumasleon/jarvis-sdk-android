package com.jarvis.features.preferences.di.data

import com.jarvis.features.preferences.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.data.repository.PreferencesRepositoryImpl
import com.jarvis.features.preferences.domain.config.PreferencesConfigProvider
import com.jarvis.features.preferences.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JarvisPreferencesDataModule {

    @Binds
    @Singleton
    abstract fun bindPreferencesConfigProvider(
        impl: PreferencesConfigProviderImpl
    ): PreferencesConfigProvider

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository

}