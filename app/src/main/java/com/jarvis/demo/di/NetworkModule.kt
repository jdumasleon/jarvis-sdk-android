package com.jarvis.demo.di

import com.jarvis.demo.data.api.FakeStoreApiService
import com.jarvis.demo.data.api.RestfulApiService
import com.jarvis.features.inspector.api.JarvisNetworkInspector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Network Module for Demo App
 * 
 * Clean baseline NetworkModule ready for interface-based DI integration
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        jarvisNetworkInspector: JarvisNetworkInspector
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .apply {
                addInterceptor(jarvisNetworkInspector.createInterceptor())
            }
            .build()
    }
    
    @Provides
    @Singleton
    fun provideDefaultRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Default base URL for general API services
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("FakeStore")
    fun provideFakeStoreRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @Named("RestfulApi")
    fun provideRestfulApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.restful-api.dev/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideFakeStoreApiService(@Named("FakeStore") retrofit: Retrofit): FakeStoreApiService {
        return retrofit.create(FakeStoreApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideRestfulApiService(@Named("RestfulApi") retrofit: Retrofit): RestfulApiService {
        return retrofit.create(RestfulApiService::class.java)
    }
}