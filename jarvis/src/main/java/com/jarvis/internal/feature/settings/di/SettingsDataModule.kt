package com.jarvis.internal.feature.settings.di

import com.jarvis.internal.feature.settings.domain.repository.SettingsRepository
import com.jarvis.internal.feature.settings.data.remote.RatingApiService
import com.jarvis.internal.feature.settings.data.repository.RatingRepositoryImpl
import com.jarvis.internal.feature.settings.data.repository.SettingsRepositoryImpl
import com.jarvis.internal.feature.settings.domain.repository.RatingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
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
        @Named("RatingApiOkHttp")
        fun provideRatingOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .build()
        }

        @Provides
        @Singleton
        @Named("RatingApi")
        fun provideRatingRetrofit(@Named("RatingApiOkHttp") okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://api.jarvis-sdk.com/") // Rating API base URL
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideRatingApiService(@Named("RatingApi") retrofit: Retrofit): RatingApiService {
            return retrofit.create(RatingApiService::class.java)
        }
    }
}