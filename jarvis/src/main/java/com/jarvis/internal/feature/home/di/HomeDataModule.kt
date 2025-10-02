@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.home.di

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.domain.performance.GetPerformanceMetricsUseCase
import com.jarvis.core.internal.domain.performance.PerformanceRepository
import com.jarvis.internal.feature.home.data.analyzer.PreferencesAnalyzer
import com.jarvis.internal.feature.home.data.repository.DashboardRepositoryImpl
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository
import com.jarvis.internal.feature.home.domain.usecase.GetDashboardMetricsUseCase
import com.jarvis.internal.feature.home.domain.usecase.RefreshDashboardMetricsUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {
    
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository
}

@Module
@InstallIn(SingletonComponent::class)
object HomeAnalyzerModule {
    
    @Provides
    @Singleton
    fun providePreferencesAnalyzer(): PreferencesAnalyzer = PreferencesAnalyzer()
}

@Module
@InstallIn(SingletonComponent::class)
object HomeUseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetDashboardMetricsUseCase(
        repository: DashboardRepository
    ): GetDashboardMetricsUseCase = GetDashboardMetricsUseCase(repository)
    
    @Provides
    @Singleton
    fun provideRefreshDashboardMetricsUseCase(
        repository: DashboardRepository
    ): RefreshDashboardMetricsUseCase = RefreshDashboardMetricsUseCase(repository)
    
    @Provides
    @Singleton
    fun provideGetPerformanceMetricsUseCase(
        repository: PerformanceRepository
    ): GetPerformanceMetricsUseCase = GetPerformanceMetricsUseCase(repository)
}