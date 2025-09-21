package com.jarvis.features.inspector.di.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jarvis.features.inspector.data.local.dao.NetworkTransactionDao
import com.jarvis.features.inspector.data.local.database.InspectorDatabase
import com.jarvis.features.inspector.data.network.JarvisNetworkCollector
import com.jarvis.features.inspector.data.network.JarvisNetworkInterceptor
import com.jarvis.features.inspector.data.repository.NetworkRepositoryImpl
import com.jarvis.features.inspector.data.repository.NetworkRulesRepositoryImpl
import com.jarvis.features.inspector.domain.repository.NetworkRepository
import com.jarvis.features.inspector.domain.repository.NetworkRulesRepository
import com.jarvis.features.inspector.domain.usecase.rules.ApplyNetworkRulesUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class JarvisInspectorDataModule {

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        networkRulesRepositoryImpl: NetworkRulesRepositoryImpl
    ): NetworkRulesRepository

    @Binds
    @Singleton
    abstract fun bindNetworkRulesRepository(
        networkRepositoryImpl: NetworkRepositoryImpl
    ): NetworkRepository

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return GsonBuilder()
                .setLenient()
                .create()
        }

        @Provides
        @Singleton
        fun provideInspectorDatabase(
            @ApplicationContext context: Context
        ): InspectorDatabase {
            return InspectorDatabase.Companion.create(context)
        }

        @Provides
        @Singleton
        fun provideNetworkTransactionDao(
            database: InspectorDatabase
        ): NetworkTransactionDao {
            return database.networkTransactionDao()
        }

        @Provides
        @Singleton
        fun provideJarvisNetworkInterceptor(
            collector: JarvisNetworkCollector,
            applyNetworkRulesUseCase: ApplyNetworkRulesUseCase
        ): JarvisNetworkInterceptor {
            return JarvisNetworkInterceptor(collector, applyNetworkRulesUseCase)
        }
    }
}