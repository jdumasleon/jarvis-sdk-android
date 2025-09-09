package com.jarvis.api

import android.app.Activity
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
import com.jarvis.api.di.JarvisSDKEntryPoint
import com.jarvis.core.navigation.JarvisSDKNavigator
import com.jarvis.api.ui.JarvisSDKApplication
import com.jarvis.api.ui.JarvisSDKFabTools
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.config.JarvisConfig
import com.jarvis.core.common.di.CoroutineDispatcherModule.IoDispatcher
import com.jarvis.core.data.performance.PerformanceManager
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.utils.ShakeDetectorEffect
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.Navigator
import com.jarvis.core.navigation.routes.JarvisSDKHomeGraph
import com.jarvis.core.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.core.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.library.BuildConfig
import com.jarvis.library.R
import com.jarvis.platform.lib.JarvisPlatform
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Main Jarvis SDK interface for initialization and configuration
 */
@ActivityRetainedScoped
class JarvisSDK @Inject constructor(
    private val configurationSynchronizer: ConfigurationSynchronizer,
    private val performanceManager: PerformanceManager,
    private val jarvisPlatform: JarvisPlatform,
    @JarvisSDKNavigator private val navigator: Navigator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private var coreInitialized = false
    private var configuration = JarvisConfig()
    private var _isJarvisActive by mutableStateOf(false)
    private var _isShowing by mutableStateOf(false)

    private lateinit var entryProviderBuilders: Set<EntryProviderInstaller>
    private var composeView: ComposeView? = null

    // Store the previous state to restore on recomposition
    private var previousJarvisActiveState = false
    private var previousShowingState = false


    fun initialize(
        config: JarvisConfig = JarvisConfig(),
        hostActivity: Activity
    ) {
        // No-op for release builds
        if (!BuildConfig.JARVIS_ENABLED) {
            configuration = config
            return
        }
        
        if (!coreInitialized) {
            configuration = config

            CoroutineScope(ioDispatcher).launch {

                configurationSynchronizer.updateConfigurations(config)
                performanceManager.initialize()
                jarvisPlatform.initialize()
                jarvisPlatform.onAppStart()
                
                // Get entry providers on main thread (UI-bound)
                withContext(Dispatchers.Main) {
                    val ep = EntryPointAccessors.fromActivity(hostActivity, JarvisSDKEntryPoint::class.java)
                    entryProviderBuilders = ep.entryProviderBuilders()
                }
            }

            _isShowing = false
            coreInitialized = true
        } else {
            // Store current state before recreating the view
            previousJarvisActiveState = _isJarvisActive
            previousShowingState = _isShowing

            composeView?.let { old ->
                (old.parent as? ViewGroup)?.removeView(old)
            }
            composeView = null
        }

        val view = ComposeView(hostActivity).apply {
            id = R.id.jarvis_compose_view
            setViewTreeLifecycleOwner(hostActivity as LifecycleOwner)
            setViewTreeViewModelStoreOwner(hostActivity as ViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(hostActivity as SavedStateRegistryOwner)

            setContent {
                val darkTheme = isSystemInDarkTheme()

                // Restore previous state if this is a recomposition after orientation change
                LaunchedEffect(Unit) {
                    if (previousJarvisActiveState) {
                        _isJarvisActive = previousJarvisActiveState
                    }
                    if (previousShowingState) {
                        _isShowing = previousShowingState
                    }
                }

                DSJarvisTheme(darkTheme = darkTheme) {

                    if (coreInitialized && _isJarvisActive) {
                        JarvisSDKFabTools(
                            onShowOverlay = {
                                navigator.goTo(JarvisSDKHomeGraph.JarvisHome)
                                _isShowing = true
                            },
                            onShowInspector = {
                                navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
                                _isShowing = true
                            },
                            onShowPreferences = {
                                navigator.goTo(JarvisSDKPreferencesGraph.JarvisPreferences)
                                _isShowing = true
                            },
                            onCloseSDK = {
                                this@JarvisSDK.deactivate()
                            },
                            isJarvisActive = this@JarvisSDK.isActive(),
                        )
                    }

                    if (this@JarvisSDK.getConfiguration().enableShakeDetection) {
                        ShakeDetectorEffect(
                            onShakeDetected = {
                                this@JarvisSDK.toggle()
                            }
                        )
                    }

                    if (_isShowing) {
                        JarvisSDKApplication(
                            navigator = navigator,
                            entryProviderBuilders = entryProviderBuilders,
                            onDismiss = {
                                this@JarvisSDK.hideOverlay()
                            }
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

    private suspend fun initPlatform() = withContext(ioDispatcher) {
        try {
            jarvisPlatform.initialize()
            jarvisPlatform.onAppStart()
        } catch (e: Exception) {
            // Proper error handling
        }
    }

    fun dismiss() {
        // No-op for release builds
        if (!BuildConfig.JARVIS_ENABLED) return
        
        // Notify platform services about app stop
        CoroutineScope(ioDispatcher).launch {
            try {
                if (jarvisPlatform.isInitialized()) {
                    jarvisPlatform.onAppStop()
                }
            } catch (e: Exception) {
                // Handle gracefully
            }
        }

        navigator.clear()
        composeView?.let { v -> (v.parent as? ViewGroup)?.removeView(v) }
        composeView = null
        _isShowing = false
        _isJarvisActive = false
    }

    fun getConfiguration(): JarvisConfig = configuration

    fun hideOverlay() {
        // No-op for release builds
        if (!BuildConfig.JARVIS_ENABLED) return
        
        navigator.clear()
        _isShowing = false
    }

    fun activate() {
        // No-op for release builds
        if (!BuildConfig.JARVIS_ENABLED) return
        
        if (coreInitialized) _isJarvisActive = true
    }

    fun deactivate() {
        // No-op for release builds
        if (!BuildConfig.JARVIS_ENABLED) return
        
        _isJarvisActive = false
        hideOverlay()
    }

    fun isActive(): Boolean {
        // Always return false for release builds
        if (!BuildConfig.JARVIS_ENABLED) return false
        
        return _isJarvisActive
    }

    fun toggle(): Boolean {
        // Always return false for release builds
        if (!BuildConfig.JARVIS_ENABLED) return false
        
        if (_isJarvisActive) deactivate() else activate()
        return _isJarvisActive
    }

    /**
     * Get access to platform services (Analytics, Crash Reporting, Feature Flags)
     * Only available after initialization
     */
    fun getPlatform(): JarvisPlatform? {
        // Always return null for release builds
        if (!BuildConfig.JARVIS_ENABLED) return null
        
        return if (coreInitialized && jarvisPlatform.isInitialized()) {
            jarvisPlatform
        } else {
            null
        }
    }
}