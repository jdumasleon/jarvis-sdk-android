package com.jarvis.demo.koin

import android.app.Application
import com.jarvis.demo.koin.di.*
import com.jarvis.features.inspector.integration.koin.jarvisInspectorKoinModule
import com.jarvis.features.preferences.integration.koin.jarvisPreferencesKoinModule
import com.jarvis.integration.koin.allJarvisKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for Koin Demo App
 *
 * PURE KOIN INTEGRATION - NO HILT REQUIRED!
 *
 * This app demonstrates how to integrate the Jarvis SDK using ONLY Koin,
 * without requiring Hilt dependencies. The SDK's Koin modules provide all
 * necessary dependencies including platform services, performance monitoring,
 * and navigation entry providers.
 */
class KoinDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin with both demo app and Jarvis SDK modules
        startKoin {
            // Enable logging for debugging
            androidLogger(Level.INFO)

            // Set Android context
            androidContext(this@KoinDemoApplication)

            // Install all modules
            modules(
                // Jarvis SDK modules - FULL Koin integration, no Hilt needed!
                *allJarvisKoinModules.toTypedArray(),

                // Feature modules from features:inspector and features:preferences
                jarvisInspectorKoinModule,
                jarvisPreferencesKoinModule,

                // Demo app modules
                networkModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
                navigationModule,
            )

            // Module check removed for simplicity
            // Can be enabled with: if (BuildConfig.DEBUG) { checkModules() }
        }
    }
}