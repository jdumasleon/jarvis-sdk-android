package com.jarvis.features.preferences.integration.koin

import com.google.gson.Gson
import com.jarvis.features.preferences.internal.data.config.PreferencesConfigProviderImpl
import com.jarvis.features.preferences.internal.data.local.PreferencesDataStoreScanner
import com.jarvis.features.preferences.internal.data.local.ProtoDataStoreScanner
import com.jarvis.features.preferences.internal.data.local.SharedPreferencesScanner
import com.jarvis.features.preferences.internal.data.repository.PreferencesRepositoryImpl
import com.jarvis.features.preferences.internal.domain.config.PreferencesConfigProvider
import com.jarvis.features.preferences.internal.domain.repository.PreferencesRepository
import com.jarvis.features.preferences.internal.domain.usecase.*
import com.jarvis.features.preferences.internal.presentation.PreferencesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for Jarvis Preferences feature
 *
 * This module provides all dependencies needed for preferences management,
 * including scanners for SharedPreferences, DataStore, and Proto DataStore.
 *
 * Usage in host app:
 * ```kotlin
 * startKoin {
 *     modules(
 *         jarvisPreferencesKoinModule,
 *         // ... other modules
 *     )
 * }
 * ```
 */
val jarvisPreferencesKoinModule = module {

    // Preferences Config Provider
    singleOf(::PreferencesConfigProviderImpl) bind PreferencesConfigProvider::class

    // Scanners
    single {
        SharedPreferencesScanner(
            context = androidContext(),
            configProvider = get()
        )
    }

    single {
        PreferencesDataStoreScanner(
            context = androidContext(),
            configProvider = get()
        )
    }

    single {
        ProtoDataStoreScanner(
            context = androidContext(),
            configProvider = get()
        )
    }

    // Repository
    single<PreferencesRepository> {
        PreferencesRepositoryImpl(
            sharedPreferencesScanner = get(),
            preferencesDataStoreScanner = get(),
            protoDataStoreScanner = get(),
            gson = get()
        )
    }

    // Use Cases
    single { GetPreferencesByStorageTypeUseCase(repository = get()) }
    single { UpdatePreferenceUseCase(repository = get()) }
    single { DeletePreferenceUseCase(repository = get()) }
    single { AddPreferenceUseCase(repository = get()) }
    single { ClearAllPreferencesUseCase(repository = get()) }
    single { GetAllPreferencesUseCase(repository = get()) }
    single { GetFilteredPreferencesUseCase(repository = get()) }
    single { ExportPreferencesUseCase(repository = get()) }
    single { ImportPreferencesUseCase(repository = get()) }

    // ViewModel
    viewModel {
        PreferencesViewModel(
            getPreferencesByStorageTypeUseCase = get(),
            updatePreferenceUseCase = get(),
            deletePreferenceUseCase = get(),
            addPreferenceUseCase = get(),
            clearAllPreferencesUseCase = get(),
            exportPreferencesUseCase = get(),
            importPreferencesUseCase = get(),
            ioDispatcher = get(named("IO"))
        )
    }
}
