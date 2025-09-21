package com.jarvis.core.data.di

import com.jarvis.core.data.performance.PerformanceManager
import com.jarvis.core.data.performance.repository.PerformanceRepositoryImpl
import com.jarvis.core.domain.performance.PerformanceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PerformanceModule {

    @Binds
    @Singleton
    abstract fun bindPerformanceRepository(
        performanceRepositoryImpl: PerformanceRepositoryImpl
    ): PerformanceRepository

    companion object {
        @Provides
        @Singleton
        fun providePerformanceManager(
            performanceRepository: PerformanceRepository
        ): PerformanceManager = PerformanceManager(performanceRepository)
    }
}