package com.jarvis.example.koin

import android.app.Application
import com.jarvis.integration.koin.jarvisKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

/**
 * Example Application class showing how to integrate Jarvis SDK with Koin
 */
class KoinExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Enable logging for debugging
            androidLogger(Level.INFO)

            // Set Android context
            androidContext(this@KoinExampleApplication)

            // Install modules
            modules(
                // Your app modules
                appModule,
                networkModule,
                repositoryModule,

                // Jarvis SDK modules - this includes all required dependencies
                *jarvisKoinModules()
            )

            // Optional: Check all dependencies can be resolved
            if (BuildConfig.DEBUG) {
                checkModules()
            }
        }
    }
}

/**
 * Example app modules showing how to structure your own dependencies
 * alongside Jarvis SDK modules
 */
val appModule = module {
    // Your app-specific dependencies
    single<AppConfiguration> { AppConfiguration() }
    single<UserPreferences> { UserPreferences(androidContext()) }
}

val networkModule = module {
    // Your network dependencies
    single<ApiService> { ApiService() }
    single<NetworkConfig> { NetworkConfig() }
}

val repositoryModule = module {
    // Your repository dependencies
    single<UserRepository> { UserRepository(get(), get()) }
    single<DataRepository> { DataRepository(get()) }
}

// Example classes (you would have your own implementations)
class AppConfiguration
class UserPreferences(context: android.content.Context)
class ApiService
class NetworkConfig
class UserRepository(apiService: ApiService, userPreferences: UserPreferences)
class DataRepository(apiService: ApiService)