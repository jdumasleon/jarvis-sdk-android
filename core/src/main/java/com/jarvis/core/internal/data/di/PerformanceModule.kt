package com.jarvis.core.internal.data.di

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.data.performance.PerformanceManager
import com.jarvis.core.internal.data.performance.repository.PerformanceRepositoryImpl
import com.jarvis.core.internal.domain.performance.PerformanceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class PerformanceModule {

    @Binds
    @Singleton
    internal abstract fun bindPerformanceRepository(
        performanceRepositoryImpl: PerformanceRepositoryImpl
    ): PerformanceRepository

    companion object {
        @Provides
        @Singleton
        internal fun providePerformanceManager(
            performanceRepository: PerformanceRepository
        ): PerformanceManager = PerformanceManager(performanceRepository)
    }
}