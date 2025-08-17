package com.jarvis.features.home.data.di

import com.jarvis.core.domain.performance.GetPerformanceMetricsUseCase
import com.jarvis.core.domain.performance.PerformanceRepository
import com.jarvis.features.home.data.analyzer.PreferencesAnalyzer
import com.jarvis.features.home.data.mapper.EnhancedDashboardMetricsMapper
import com.jarvis.features.home.data.repository.DashboardRepositoryImpl
import com.jarvis.features.home.domain.repository.DashboardRepository
import com.jarvis.features.home.domain.usecase.GetDashboardMetricsUseCase
import com.jarvis.features.home.domain.usecase.RefreshDashboardMetricsUseCase
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