package com.jarvis.api

import android.app.Activity
import android.os.StrictMode
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jarvis.internal.di.JarvisSDKEntryProvider
import com.jarvis.core.internal.navigation.JarvisSDKNavigator
import com.jarvis.internal.ui.JarvisSDKApplication
import com.jarvis.internal.ui.JarvisSDKFabTools
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.config.JarvisConfig
import com.jarvis.core.internal.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.internal.data.performance.PerformanceManager
import com.jarvis.internal.data.work.NetworkCleanupScheduler
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.designsystem.utils.ShakeDetectorEffect
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.navigation.routes.JarvisSDKHomeGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.library.R
import com.jarvis.core.internal.platform.JarvisPlatform
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ActivityRetainedScoped
class JarvisSDK @Inject constructor(
    private val configurationSynchronizer: ConfigurationSynchronizer,
    private val performanceManager: PerformanceManager,
    private val jarvisPlatform: JarvisPlatform,
    private val networkCleanupScheduler: NetworkCleanupScheduler,
    @JarvisSDKNavigator private val navigator: Navigator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private var coreInitialized = false
    private var configuration = JarvisConfig()
    private var _isJarvisActive by mutableStateOf(false)
    private val _activeState = MutableStateFlow(false)
    val activeState: StateFlow<Boolean> = _activeState.asStateFlow()
    private var _isShowing by mutableStateOf(false)

    /**
     * Update Jarvis visibility state and pause/resume performance collection accordingly
     */
    private fun setIsShowing(showing: Boolean) {
        if (_isShowing != showing) {
            _isShowing = showing

            // Pause/resume performance collection based on Jarvis visibility
            if (showing) {
                // Jarvis opened - pause collection and capture session snapshot
                CoroutineScope(ioDispatcher).launch {
                    performanceManager.pauseCollection()
                    android.util.Log.d("JarvisSDK", "Jarvis opened - paused and captured session snapshot")
                }
            } else {
                // Jarvis closed - resume collection to monitor host app
                performanceManager.resumeCollection()
                android.util.Log.d("JarvisSDK", "Jarvis closed - resuming performance collection")
            }
        }
    }

    private lateinit var entryProviderBuilders: Set<EntryProviderInstaller>
    private var composeView: ComposeView? = null

    private var previousJarvisActiveState = false
    private var previousShowingState = false

    suspend fun initialize(
        config: JarvisConfig = JarvisConfig(),
        hostActivity: Activity
    ) {
        if (!coreInitialized) {
            configuration = config

            withContext(ioDispatcher) {
                val old = StrictMode.allowThreadDiskReads()
                try {
                    configurationSynchronizer.updateConfigurations(config)
                    performanceManager.initialize()
                    jarvisPlatform.initialize()
                    jarvisPlatform.onAppStart()

                    // Schedule periodic cleanup of old network requests
                    networkCleanupScheduler.scheduleCleanup()
                } finally {
                    StrictMode.setThreadPolicy(old)
                }
            }

            withContext(Dispatchers.Main) {
                val ep = EntryPointAccessors.fromActivity(
                    hostActivity,
                    JarvisSDKEntryProvider::class.java
                )
                entryProviderBuilders = ep.entryProviderBuilders()
            }

            setIsShowing(false)
            coreInitialized = true
        } else {
            previousJarvisActiveState = _isJarvisActive
            previousShowingState = _isShowing

            composeView?.let { old ->
                (old.parent as? ViewGroup)?.removeView(old)
            }
            composeView = null
        }

        withContext(Dispatchers.Main) {
            val view = ComposeView(hostActivity).apply {
                id = R.id.jarvis_compose_view
                setViewTreeLifecycleOwner(hostActivity as LifecycleOwner)
                setViewTreeViewModelStoreOwner(hostActivity as ViewModelStoreOwner)
                setViewTreeSavedStateRegistryOwner(hostActivity as SavedStateRegistryOwner)

                setContent {
                    val darkTheme = isSystemInDarkTheme()

                    LaunchedEffect(Unit) {
                        if (previousJarvisActiveState) setJarvisActive(previousJarvisActiveState)
                        if (previousShowingState) setIsShowing(previousShowingState)
                    }

                    DSJarvisTheme(darkTheme = darkTheme) {

                        if (coreInitialized && _isJarvisActive) {
                            JarvisSDKFabTools(
                                onShowOverlay = {
                                    navigator.goTo(JarvisSDKHomeGraph.JarvisHome)
                                    setIsShowing(true)
                                },
                                onShowInspector = {
                                    navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
                                    setIsShowing(true)
                                },
                                onShowPreferences = {
                                    navigator.goTo(JarvisSDKPreferencesGraph.JarvisPreferences)
                                    setIsShowing(true)
                                },
                                onCloseSDK = { this@JarvisSDK.deactivate() },
                                isJarvisActive = this@JarvisSDK.isActive(),
                            )
                        }

                        if (this@JarvisSDK.getConfiguration().enableShakeDetection) {
                            ShakeDetectorEffect(
                                onShakeDetected = { this@JarvisSDK.toggle() }
                            )
                        }

                        if (_isShowing) {
                            JarvisSDKApplication(
                                navigator = navigator,
                                entryProviderBuilders = entryProviderBuilders,
                                onDismiss = { this@JarvisSDK.hideOverlay() }
                            )
                        }
                    }
                }
            }

            hostActivity.addContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            composeView = view
        }
    }

    fun initializeAsync(
        config: JarvisConfig = JarvisConfig(),
        hostActivity: Activity,
        scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    ): Job = scope.launch {
        initialize(config, hostActivity)
    }

    /**
     * Initialize SDK with externally-provided entry providers (for Koin integration).
     * This method bypasses Hilt's EntryPointAccessors and instead accepts entry providers
     * directly, allowing Koin-based apps to integrate the SDK without Hilt.
     *
     * @param config SDK configuration
     * @param hostActivity The host activity
     * @param entryProviders Set of entry provider installers from Koin
     */
    suspend fun initializeWithEntryProviders(
        config: JarvisConfig = JarvisConfig(),
        hostActivity: Activity,
        entryProviders: Set<EntryProviderInstaller>
    ) {
        if (!coreInitialized) {
            configuration = config

            withContext(ioDispatcher) {
                val old = StrictMode.allowThreadDiskReads()
                try {
                    configurationSynchronizer.updateConfigurations(config)
                    performanceManager.initialize()
                    jarvisPlatform.initialize()
                    jarvisPlatform.onAppStart()

                    // Schedule periodic cleanup of old network requests
                    networkCleanupScheduler.scheduleCleanup()
                } finally {
                    StrictMode.setThreadPolicy(old)
                }
            }

            withContext(Dispatchers.Main) {
                // Use provided entry providers instead of getting them from Hilt
                entryProviderBuilders = entryProviders
            }

            setIsShowing(false)
            coreInitialized = true
        } else {
            previousJarvisActiveState = _isJarvisActive
            previousShowingState = _isShowing

            composeView?.let { old ->
                (old.parent as? ViewGroup)?.removeView(old)
            }
            composeView = null
        }

        withContext(Dispatchers.Main) {
            val view = ComposeView(hostActivity).apply {
                id = R.id.jarvis_compose_view
                setViewTreeLifecycleOwner(hostActivity as LifecycleOwner)
                setViewTreeViewModelStoreOwner(hostActivity as ViewModelStoreOwner)
                setViewTreeSavedStateRegistryOwner(hostActivity as SavedStateRegistryOwner)

                setContent {
                    val darkTheme = isSystemInDarkTheme()

                    LaunchedEffect(Unit) {
                        if (previousJarvisActiveState) setJarvisActive(previousJarvisActiveState)
                        if (previousShowingState) setIsShowing(previousShowingState)
                    }

                    DSJarvisTheme(darkTheme = darkTheme) {

                        if (coreInitialized && _isJarvisActive) {
                            JarvisSDKFabTools(
                                onShowOverlay = {
                                    navigator.goTo(JarvisSDKHomeGraph.JarvisHome)
                                    setIsShowing(true)
                                },
                                onShowInspector = {
                                    navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
                                    setIsShowing(true)
                                },
                                onShowPreferences = {
                                    navigator.goTo(JarvisSDKPreferencesGraph.JarvisPreferences)
                                    setIsShowing(true)
                                },
                                onCloseSDK = { this@JarvisSDK.deactivate() },
                                isJarvisActive = this@JarvisSDK.isActive(),
                            )
                        }

                        if (this@JarvisSDK.getConfiguration().enableShakeDetection) {
                            ShakeDetectorEffect(
                                onShakeDetected = { this@JarvisSDK.toggle() }
                            )
                        }

                        if (_isShowing) {
                            JarvisSDKApplication(
                                navigator = navigator,
                                entryProviderBuilders = entryProviderBuilders,
                                onDismiss = { this@JarvisSDK.hideOverlay() }
                            )
                        }
                    }
                }
            }

            hostActivity.addContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            composeView = view
        }
    }

    fun dismiss() {
        CoroutineScope(ioDispatcher).launch {
            try {
                if (jarvisPlatform.isInitialized()) {
                    jarvisPlatform.onAppStop()
                }
            } catch (_: Exception) { }
        }

        navigator.clear()
        composeView?.let { v -> (v.parent as? ViewGroup)?.removeView(v) }
        composeView = null
        setIsShowing(false)
        setJarvisActive(false)
    }

    fun getConfiguration(): JarvisConfig = configuration

    fun hideOverlay() {
        navigator.clear()
        setIsShowing(false)
    }

    fun activate() { if (coreInitialized) setJarvisActive(true) }

    fun deactivate() {
        setJarvisActive(false)
        hideOverlay()
    }

    fun isActive(): Boolean = _isJarvisActive

    fun toggle(): Boolean { if (_isJarvisActive) deactivate() else activate(); return _isJarvisActive }

    fun getPlatform(): JarvisPlatform? =
        if (coreInitialized && jarvisPlatform.isInitialized()) jarvisPlatform else null

    fun observeActiveState(): StateFlow<Boolean> = activeState

    private fun setJarvisActive(active: Boolean) {
        if (_isJarvisActive == active) return
        _isJarvisActive = active
        _activeState.value = active
    }
}
