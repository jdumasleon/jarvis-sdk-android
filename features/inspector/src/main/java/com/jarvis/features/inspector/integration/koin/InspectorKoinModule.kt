package com.jarvis.features.inspector.integration.koin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jarvis.features.inspector.api.JarvisNetworkInspector
import com.jarvis.features.inspector.internal.data.local.database.InspectorDatabase
import com.jarvis.features.inspector.internal.data.mapper.NetworkTransactionMapper
import com.jarvis.features.inspector.internal.data.network.JarvisNetworkCollector
import com.jarvis.features.inspector.internal.data.network.JarvisNetworkInterceptor
import com.jarvis.features.inspector.internal.data.repository.NetworkRepositoryImpl
import com.jarvis.features.inspector.internal.data.repository.NetworkRulesRepositoryImpl
import com.jarvis.features.inspector.internal.domain.repository.NetworkRepository
import com.jarvis.features.inspector.internal.domain.repository.NetworkRulesRepository
import com.jarvis.features.inspector.internal.domain.usecase.rules.ApplyNetworkRulesUseCase
import com.jarvis.features.inspector.internal.domain.usecase.rules.GetNetworkRulesUseCase
import com.jarvis.features.inspector.internal.domain.usecase.rules.ManageNetworkRulesUseCase
import com.jarvis.features.inspector.internal.presentation.breakpoints.NetworkBreakpointsViewModel
import com.jarvis.features.inspector.internal.presentation.transactions.NetworkInspectorViewModel
import com.jarvis.features.inspector.internal.presentation.transactionsDetails.NetworkTransactionDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for Jarvis Network Inspector feature
 *
 * This module provides all dependencies needed for network inspection,
 * including Room database, repositories, and the public JarvisNetworkInspector API.
 *
 * Usage in host app:
 * ```kotlin
 * startKoin {
 *     modules(
 *         jarvisInspectorKoinModule,
 *         // ... other modules
 *     )
 * }
 * ```
 */
val jarvisInspectorKoinModule = module {

    // Gson for JSON parsing
    single<Gson>(named("jarvis_inspector")) {
        GsonBuilder()
            .setLenient()
            .create()
    }

    // Inspector Database (Room)
    single<InspectorDatabase> {
        InspectorDatabase.create(androidContext())
    }

    // Network Transaction DAO
    single {
        get<InspectorDatabase>().networkTransactionDao()
    }

    // Network Transaction Mapper
    single {
        NetworkTransactionMapper(gson = get(named("jarvis_inspector")))
    }

    // Repositories
    single<NetworkRulesRepository> {
        NetworkRulesRepositoryImpl(
            context = androidContext()
        )
    }

    single<NetworkRepository> {
        NetworkRepositoryImpl(
            dao = get(),
            mapper = get()
        )
    }

    // Use Cases
    single {
        ApplyNetworkRulesUseCase(
            repository = get<NetworkRulesRepository>()
        )
    }

    single {
        GetNetworkRulesUseCase(
            repository = get<NetworkRulesRepository>()
        )
    }

    single {
        ManageNetworkRulesUseCase(
            repository = get<NetworkRulesRepository>()
        )
    }

    // Network Collector
    single {
        JarvisNetworkCollector(
            networkRepository = get()
        )
    }

    // Network Interceptor
    single {
        JarvisNetworkInterceptor(
            collector = get(),
            applyNetworkRulesUseCase = get()
        )
    }

    // Network Inspector (public API)
    single {
        JarvisNetworkInspector(
            context = androidContext(),
            networkCollector = get(),
            networkInterceptor = get()
        )
    }

    // ViewModels
    viewModel {
        NetworkInspectorViewModel(
            networkRepository = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    viewModel {
        NetworkTransactionDetailViewModel(
            networkRepository = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    viewModel {
        NetworkBreakpointsViewModel(
            getRulesUseCase = get(),
            manageRulesUseCase = get(),
            repository = get(),
            ioDispatcher = get(named("IO"))
        )
    }
}
