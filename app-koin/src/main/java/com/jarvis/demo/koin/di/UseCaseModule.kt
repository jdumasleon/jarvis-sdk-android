package com.jarvis.demo.koin.di

import com.jarvis.demo.koin.domain.usecase.home.ManageJarvisModeUseCase
import com.jarvis.demo.koin.domain.usecase.home.RefreshDataUseCase
import com.jarvis.demo.koin.domain.usecase.inspector.PerformApiCallsUseCase
import com.jarvis.demo.koin.domain.usecase.preferences.*
import org.koin.dsl.module

/**
 * Use Case Module for Koin Demo App
 * Provides all use case implementations
 */
val useCaseModule = module {

    // Home Use Cases
    factory<ManageJarvisModeUseCase> {
        ManageJarvisModeUseCase(
            jarvisSDK = get()
        )
    }

    factory<RefreshDataUseCase> {
        RefreshDataUseCase(
            demoHomeRepository = get()
        )
    }

    // Inspector Use Cases
    factory<PerformApiCallsUseCase> {
        PerformApiCallsUseCase(
            repository = get()
        )
    }

    // Preferences Use Cases
    factory<GetAllPreferencesUseCase> {
        GetAllPreferencesUseCase(
            repository = get()
        )
    }

    factory<GetSharedPreferencesUseCase> {
        GetSharedPreferencesUseCase(
            repository = get()
        )
    }

    factory<GetDataStorePreferencesUseCase> {
        GetDataStorePreferencesUseCase(
            repository = get()
        )
    }

    factory<GetProtoDataStorePreferencesUseCase> {
        GetProtoDataStorePreferencesUseCase(
            repository = get()
        )
    }

    factory<ManagePreferencesUseCase> {
        ManagePreferencesUseCase(
            repository = get()
        )
    }
}