@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.preferences.internal.di.data

import androidx.annotation.RestrictTo

import com.jarvis.features.preferences.internal.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.internal.data.repository.PreferencesRepositoryImpl
import com.jarvis.features.preferences.internal.domain.config.PreferencesConfigProvider
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
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
    internal abstract fun bindPreferencesConfigProvider(
        impl: PreferencesConfigProviderImpl
    ): PreferencesConfigProvider

    @Binds
    @Singleton
    internal abstract fun bindPreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository

}