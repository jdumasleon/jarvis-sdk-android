@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.settings.di

import androidx.annotation.RestrictTo

import com.jarvis.library.BuildConfig
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
import okhttp3.ResponseBody.Companion.toResponseBody
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
                .baseUrl(BuildConfig.RATING_API_BASE_URL)
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