package com.jarvis.features.settings.data.di

import com.jarvis.features.settings.data.remote.RatingApiService
import com.jarvis.features.settings.data.repository.RatingRepositoryImpl
import com.jarvis.features.settings.data.repository.SettingsRepositoryImpl
import com.jarvis.features.settings.domain.repository.RatingRepository
import com.jarvis.features.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindRatingRepository(
        ratingRepositoryImpl: RatingRepositoryImpl
    ): RatingRepository

    companion object {
        @Provides
        @Singleton
        fun provideRatingApiService(retrofit: Retrofit): RatingApiService {
            return retrofit.create(RatingApiService::class.java)
        }
    }
}