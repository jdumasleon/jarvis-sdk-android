package com.jarvis.demo.koin.di

import com.jarvis.demo.koin.presentation.home.HomeViewModel
import com.jarvis.demo.koin.presentation.inspector.InspectorViewModel
import com.jarvis.demo.koin.presentation.preferences.PreferencesViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ViewModel Module for Koin Demo App
 * Provides all ViewModel implementations with viewModel scope
 */
val viewModelModule = module {
    single<CoroutineDispatcher>(named("IO")) { Dispatchers.IO }
    single<CoroutineDispatcher>(named("Default")) { Dispatchers.Default }
    single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("Unconfined")) { Dispatchers.Unconfined }

    // Home ViewModel
    viewModel<HomeViewModel> {
        HomeViewModel(
            manageJarvisModeUseCase = get(),
            refreshDataUseCase = get()
        )
    }

    // Inspector ViewModel
    viewModel<InspectorViewModel> {
        InspectorViewModel(
            performApiCallsUseCase = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    // Preferences ViewModel
    viewModel<PreferencesViewModel> {
        PreferencesViewModel(
            getSharedPreferencesUseCase = get(),
            getDataStorePreferencesUseCase = get(),
            getProtoDataStorePreferencesUseCase = get(),
            managePreferencesUseCase = get(),
            ioDispatcher = get(named("IO"))
        )
    }
}