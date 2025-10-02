package com.jarvis.integration.koin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation3.runtime.entry
import com.google.gson.Gson
import com.jarvis.api.JarvisSDK
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.core.internal.common.network.NetworkMonitor
import com.jarvis.core.internal.common.network.NetworkMonitorManager
import com.jarvis.core.internal.data.performance.PerformanceManager
import com.jarvis.core.internal.data.performance.monitor.*
import com.jarvis.core.internal.data.performance.repository.PerformanceRepositoryImpl
import com.jarvis.core.internal.data.preferences.datasource.JarvisInternalPreferencesDataSource
import com.jarvis.core.internal.data.preferences.repository.JarvisInternalPreferencesRepositoryImpl
import com.jarvis.core.internal.domain.performance.PerformanceRepository
import com.jarvis.core.internal.domain.preferences.repository.JarvisInternalPreferencesRepository
import com.jarvis.core.internal.domain.preferences.usecase.ManageHeaderContentStateUseCase
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.navigation.routes.*
import com.jarvis.core.internal.platform.JarvisPlatform
import com.jarvis.core.internal.platform.analytics.Analytics
import com.jarvis.core.internal.platform.analytics.PostHogAnalytics
import com.jarvis.core.internal.platform.crash.CrashReporter
import com.jarvis.core.internal.platform.crash.SentryCrashReporter
import com.jarvis.core.internal.platform.featureflags.FeatureFlags
import com.jarvis.core.internal.platform.featureflags.PostHogFeatureFlags
import com.jarvis.features.inspector.internal.presentation.breakpoints.NetworkBreakpointsScreen
import com.jarvis.features.inspector.internal.presentation.transactions.NetworkInspectorRoute
import com.jarvis.features.inspector.internal.presentation.transactionsDetails.NetworkTransactionDetailRoute
import com.jarvis.features.preferences.internal.presentation.PreferencesRoute
import com.jarvis.internal.feature.home.data.analyzer.PreferencesAnalyzer
import com.jarvis.internal.feature.home.data.mapper.DashboardMetricsMapper
import com.jarvis.internal.feature.home.data.mapper.EnhancedDashboardMetricsMapper
import com.jarvis.internal.feature.home.data.mapper.HealthScoreCalculator
import com.jarvis.internal.feature.home.data.mapper.NetworkAnalyzer
import com.jarvis.internal.feature.home.data.repository.DashboardRepositoryImpl
import com.jarvis.internal.feature.home.domain.repository.DashboardRepository
import com.jarvis.internal.feature.home.domain.usecase.GetDashboardMetricsUseCase
import com.jarvis.internal.feature.home.domain.usecase.RefreshDashboardMetricsUseCase
import com.jarvis.internal.feature.home.presentation.HomeRoute
import com.jarvis.internal.feature.home.presentation.HomeViewModel
import com.jarvis.internal.feature.settings.data.remote.RatingApiService
import com.jarvis.internal.feature.settings.data.repository.RatingRepositoryImpl
import com.jarvis.internal.feature.settings.data.repository.SettingsRepositoryImpl
import com.jarvis.internal.feature.settings.domain.repository.RatingRepository
import com.jarvis.internal.feature.settings.domain.repository.SettingsRepository
import com.jarvis.internal.feature.settings.domain.usecase.GetAppInfoUseCase
import com.jarvis.internal.feature.settings.domain.usecase.GetSettingsAppInfoUseCase
import com.jarvis.internal.feature.settings.domain.usecase.GetSettingsItemsUseCase
import com.jarvis.internal.feature.settings.domain.usecase.SubmitRatingUseCase
import com.jarvis.internal.feature.settings.presentation.SettingsRoute
import com.jarvis.internal.feature.settings.presentation.SettingsViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

// DataStore extensions
private val Context.platformDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "platform_preferences"
)

private val Context.jarvisInternalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jarvis_internal_preferences"
)

/**
 * Complete Koin integration modules for Jarvis SDK
 *
 * These modules provide FULL Koin integration for the Jarvis SDK, allowing
 * external Koin-based apps to use the SDK WITHOUT requiring Hilt.
 *
 * Usage:
 * ```kotlin
 * startKoin {
 *     modules(
 *         // Your app modules
 *         *allJarvisKoinModules.toTypedArray()
 *     )
 * }
 * ```
 *
 * Then in your Activity:
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     private val jarvisSDK: JarvisSDK by inject()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         jarvisSDK.initializeWithKoin(config, this)
 *     }
 * }
 * ```
 */

/**
 * Core Koin module - provides coroutine dispatchers and network monitoring
 */
val jarvisCoreKoinModule = module {
    single<CoroutineDispatcher>(named("IO")) { Dispatchers.IO }
    single<CoroutineDispatcher>(named("Default")) { Dispatchers.Default }
    single<CoroutineDispatcher>(named("Main")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("Unconfined")) { Dispatchers.Unconfined }

    single<NetworkMonitor> {
        NetworkMonitorManager(
            context = androidContext(),
            ioDispatcher = get(named("IO"))
        )
    }
}

/**
 * Platform Koin module - provides platform services (analytics, crash reporting, feature flags)
 */
val jarvisPlatformKoinModule = module {
    // DataStores
    single<DataStore<Preferences>>(named("platform")) {
        androidContext().platformDataStore
    }
    single<DataStore<Preferences>>(named("jarvis_internal")) {
        androidContext().jarvisInternalDataStore
    }

    // Platform services
    single<Analytics> {
        PostHogAnalytics(
            context = androidContext(),
            ioDispatcher = get(named("IO"))
        )
    }
    single<CrashReporter> {
        SentryCrashReporter(
            ioDispatcher = get(named("IO"))
        )
    }
    single<FeatureFlags> {
        PostHogFeatureFlags(
            dataStore = get(named("platform")),
            ioDispatcher = get(named("IO"))
        )
    }

    // Platform
    single<JarvisPlatform> {
        JarvisPlatform(
            analytics = get(),
            crashReporter = get(),
            featureFlags = get()
        )
    }

    // Internal preferences
    single<JarvisInternalPreferencesDataSource> {
        JarvisInternalPreferencesDataSource(get(named("jarvis_internal")))
    }
    single<JarvisInternalPreferencesRepository> {
        JarvisInternalPreferencesRepositoryImpl(get())
    }

    // Internal preferences use cases
    single {
        ManageHeaderContentStateUseCase(
            internalPreferencesRepository = get()
        )
    }
}

/**
 * Performance Koin module - provides performance monitoring
 */
val jarvisPerformanceKoinModule = module {
    // Performance monitors
    single { CpuMonitor() }
    single { MemoryMonitor(androidContext()) }
    single { FpsMonitor() }
    single { ModuleLoadMonitor() }
    single { JankMonitor() }
    single { Gson() }

    // Performance Repository
    single<PerformanceRepository> {
        PerformanceRepositoryImpl(
            cpuMonitor = get(),
            memoryMonitor = get(),
            fpsMonitor = get(),
            moduleLoadMonitor = get(),
            jankMonitor = get(),
            gson = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    // Performance Manager
    single { PerformanceManager(get()) }
}

/**
 * Inspector Koin module - PLACEHOLDER
 *
 * NOTE: Inspector dependencies are provided by the features:inspector module itself.
 * Host apps should include `jarvisInspectorKoinModule` from:
 * com.jarvis.features.inspector.integration.koin.jarvisInspectorKoinModule
 *
 * This module is empty and kept for compatibility.
 */
val jarvisInspectorKoinModule = module {
    // Empty - inspector dependencies are in features:inspector module
}

/**
 * ViewModels Koin module - provides SDK internal ViewModels and their dependencies
 */
val jarvisViewModelsKoinModule = module {

    // Home feature dependencies
    single { PreferencesAnalyzer() }
    single { NetworkAnalyzer() }
    single { HealthScoreCalculator() }
    single { DashboardMetricsMapper() }
    single {
        EnhancedDashboardMetricsMapper(
            basicMapper = get(),
            healthScoreCalculator = get(),
            networkAnalyzer = get(),
            preferencesAnalyzer = get()
        )
    }

    single<DashboardRepository> {
        DashboardRepositoryImpl(
            networkRepository = get(),
            preferencesRepository = get(),
            mapper = get(),
            enhancedMapper = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    single { GetDashboardMetricsUseCase(repository = get()) }
    single { RefreshDashboardMetricsUseCase(repository = get()) }
    single {
        com.jarvis.core.internal.domain.performance.GetPerformanceMetricsUseCase(
            repository = get()
        )
    }

    // Home ViewModel
    viewModel {
        HomeViewModel(
            dashboardRepository = get(),
            getPerformanceMetricsUseCase = get(),
            manageHeaderContentStateUseCase = get(),
            ioDispatcher = get(named("IO"))
        )
    }

    // Settings feature dependencies
    single<OkHttpClient>(named("RatingApiOkHttp")) {
        OkHttpClient.Builder().build()
    }

    single<Retrofit>(named("RatingApi")) {
        Retrofit.Builder()
            .baseUrl("https://api.jarvis-sdk.com/")
            .client(get(named("RatingApiOkHttp")))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<RatingApiService> {
        get<Retrofit>(named("RatingApi")).create(RatingApiService::class.java)
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            context = androidContext(),
            ioDispatcher = get(named("IO"))
        )
    }

    single<RatingRepository> {
        RatingRepositoryImpl(
            ratingApiService = get()
        )
    }

    single { GetAppInfoUseCase(settingsRepository = get()) }
    single { GetSettingsItemsUseCase(settingsRepository = get()) }
    single { GetSettingsAppInfoUseCase(settingsRepository = get()) }
    single { SubmitRatingUseCase(ratingRepository = get()) }

    // Settings ViewModel
    viewModel {
        SettingsViewModel(
            getAppInfoUseCase = get(),
            getSettingsItemsUseCase = get(),
            getSettingsAppInfoUseCase = get(),
            submitRatingUseCase = get(),
            ioDispatcher = get(named("IO"))
        )
    }
}

/**
 * Navigation Koin module - provides SDK navigation and entry providers
 */
val jarvisNavigationKoinModule = module {
    single<Navigator>(named("jarvis_sdk")) {
        Navigator().apply {
            initialize(JarvisSDKHomeGraph.JarvisHome)
        }
    }

    // Entry provider installers - these register the navigation routes
    single<Set<EntryProviderInstaller>>(named("jarvis_entry_providers")) {
        setOf(
            // Home entry provider
            { navigator ->
                entry<JarvisSDKHomeGraph.JarvisHome> {
                    HomeRoute(viewModel = org.koin.androidx.compose.koinViewModel())
                }
            },
            // Settings entry provider
            { navigator ->
                entry<JarvisSDKSettingsGraph.JarvisSettings> {
                    SettingsRoute(
                        viewModel = org.koin.androidx.compose.koinViewModel(),
                        onNavigateToPreferences = {
                            navigator.goTo(JarvisSDKPreferencesGraph.JarvisPreferences)
                        },
                        onNavigateToInspector = {
                            navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
                        },
                        onNavigateToLogging = {
                            // TODO: Implement logging navigation
                        }
                    )
                }
            },
            // Inspector entry providers
            { navigator ->
                entry<JarvisSDKInspectorGraph.JarvisInspectorTransactions> {
                    NetworkInspectorRoute(
                        viewModel = org.koin.androidx.compose.koinViewModel(),
                        onNavigateToDetail = { id ->
                            navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail(id))
                        },
                        onNavigateToRules = {
                            navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorBreakpoints)
                        }
                    )
                }

                entry<JarvisSDKInspectorGraph.JarvisInspectorTransactionDetail> { args ->
                    NetworkTransactionDetailRoute(
                        transactionId = args.transactionId,
                        viewModel = org.koin.androidx.compose.koinViewModel()
                    )
                }

                entry<JarvisSDKInspectorGraph.JarvisInspectorBreakpoints> {
                    NetworkBreakpointsScreen(
                        actionKey = JarvisSDKInspectorGraph.JarvisInspectorBreakpoints.actionKey,
                        viewModel = org.koin.androidx.compose.koinViewModel()
                    )
                }
            },
            // Preferences entry provider
            {
                entry<JarvisSDKPreferencesGraph.JarvisPreferences> {
                    PreferencesRoute(
                        viewModel = org.koin.androidx.compose.koinViewModel()
                    )
                }
            }
        )
    }
}

/**
 * Preferences Koin module - PLACEHOLDER
 *
 * NOTE: Preferences dependencies are provided by the features:preferences module itself.
 * Host apps should include `jarvisPreferencesKoinModule` from:
 * com.jarvis.features.preferences.integration.koin.jarvisPreferencesKoinModule
 *
 * This module is empty and kept for compatibility.
 */
val jarvisPreferencesKoinModule = module {
    // Empty - preferences dependencies are in features:preferences module
}

/**
 * SDK Koin module - provides configuration synchronizer and JarvisSDK instance
 *
 * NOTE: ConfigurationSynchronizer requires PreferencesConfigProvider which must be
 * provided by including jarvisPreferencesKoinModule from features:preferences.
 */
val jarvisSDKKoinModule = module {
    single<ConfigurationSynchronizer> {
        ConfigurationSynchronizer(
            preferencesConfigProvider = get()
        )
    }

    single<JarvisSDK> {
        JarvisSDK(
            configurationSynchronizer = get(),
            performanceManager = get(),
            jarvisPlatform = get(),
            navigator = get(named("jarvis_sdk")),
            ioDispatcher = get(named("IO"))
        )
    }
}

/**
 * Complete list of all Jarvis SDK Koin modules.
 * Include this in your Koin configuration to enable Jarvis SDK with Koin.
 */
val allJarvisKoinModules = listOf(
    jarvisCoreKoinModule,
    jarvisPlatformKoinModule,
    jarvisPerformanceKoinModule,
    jarvisInspectorKoinModule,
    jarvisViewModelsKoinModule,
    jarvisNavigationKoinModule,
    jarvisPreferencesKoinModule,
    jarvisSDKKoinModule
)
