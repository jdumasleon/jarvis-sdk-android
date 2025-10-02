package com.jarvis.demo.koin.di

import com.jarvis.demo.koin.data.api.FakeStoreApiService
import com.jarvis.demo.koin.data.preferences.DemoPreferencesRepository
import com.jarvis.demo.koin.data.preferences.PreferencesDataStoreManager
import com.jarvis.demo.koin.data.preferences.ProtoDataStoreManager
import com.jarvis.demo.koin.data.preferences.SharedPreferencesManager
import com.jarvis.demo.koin.data.repository.DemoApiRepository
import com.jarvis.demo.koin.data.repository.DemoHomeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Repository Module for Koin Demo App
 * Provides all repository implementations and data managers
 */
val repositoryModule = module {

    // Preferences Managers
    single<SharedPreferencesManager> {
        SharedPreferencesManager(androidContext())
    }

    single<PreferencesDataStoreManager> {
        PreferencesDataStoreManager(androidContext())
    }

    single<ProtoDataStoreManager> {
        ProtoDataStoreManager(androidContext())
    }

    // Preferences Repository
    single<DemoPreferencesRepository> {
        DemoPreferencesRepository(
            sharedPreferencesManager = get(),
            preferencesDataStoreManager = get(),
            protoDataStoreManager = get()
        )
    }

    // API Repository
    single<DemoApiRepository> {
        DemoApiRepository(
            fakeStoreApi = get(),
            restfulApi = get()
        )
    }

    // Home Repository
    single<DemoHomeRepository> {
        DemoHomeRepository(
            fakeStoreApiService = get(),
            restfulApiService = get()
        )
    }
}