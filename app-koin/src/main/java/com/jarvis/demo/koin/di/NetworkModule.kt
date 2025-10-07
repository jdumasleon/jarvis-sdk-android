package com.jarvis.demo.koin.di

import com.jarvis.demo.koin.data.api.FakeStoreApiService
import com.jarvis.demo.koin.data.api.RestfulApiService
import com.jarvis.features.inspector.api.JarvisNetworkInspector
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Network Module for Koin Demo App
 * Converted from Hilt to Koin for dependency injection
 */
val networkModule = module {

    // HTTP Logging Interceptor
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // OkHttp Client
    single<OkHttpClient> {
        val loggingInterceptor: HttpLoggingInterceptor = get()
        val jarvisNetworkInspector: JarvisNetworkInspector = get()

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(jarvisNetworkInspector.createInterceptor())
            .build()
    }

    // Default Retrofit instance
    single<Retrofit> {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .baseUrl("https://api.example.com/") // Default base URL for general API services
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // FakeStore Retrofit instance
    single<Retrofit>(named("FakeStore")) {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .baseUrl("https://fakestoreapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // RestfulApi Retrofit instance
    single<Retrofit>(named("RestfulApi")) {
        val okHttpClient: OkHttpClient = get()
        Retrofit.Builder()
            .baseUrl("https://api.restful-api.dev/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // FakeStore API Service
    single<FakeStoreApiService> {
        val retrofit: Retrofit = get(named("FakeStore"))
        retrofit.create(FakeStoreApiService::class.java)
    }

    // RestfulApi Service
    single<RestfulApiService> {
        val retrofit: Retrofit = get(named("RestfulApi"))
        retrofit.create(RestfulApiService::class.java)
    }
}