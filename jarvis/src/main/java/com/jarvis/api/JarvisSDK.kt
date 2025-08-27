package com.jarvis.api

import android.app.Activity
import android.content.Context
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
import com.jarvis.api.ui.JarvisSDKApplication
import com.jarvis.api.ui.JarvisSDKFabTools
import com.jarvis.config.ConfigurationSynchronizer
import com.jarvis.config.JarvisConfig
import com.jarvis.core.data.performance.PerformanceManager
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.utils.ShakeDetectorEffect
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import com.jarvis.features.inspector.lib.navigation.JarvisSDKInspectorGraph
import com.jarvis.features.preferences.lib.navigation.JarvisSDKPreferencesGraph
import com.jarvis.library.R
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main Jarvis SDK interface for initialization and configuration
 */
@Singleton
class JarvisSDK @Inject constructor(
    private val configurationSynchronizer: ConfigurationSynchronizer,
    private val performanceManager: PerformanceManager
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

    private val navigator: Navigator = Navigator()
    private val route: NavigationRoute = JarvisSDKHomeGraph.JarvisHome

    fun initialize(
        config: JarvisConfig = JarvisConfig(),
        hostActivity: Activity
    ) {

        if (!coreInitialized) {
            configuration = config
            configurationSynchronizer.updateConfigurations(config)
            performanceManager.initialize()

            val ep = EntryPointAccessors.fromActivity(hostActivity, JarvisSDKEntryPoint::class.java)
            entryProviderBuilders = ep.entryProviderBuilders()

            navigator.initialize(route)
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

    fun dismiss() {
        navigator.clear()
        composeView?.let { v -> (v.parent as? ViewGroup)?.removeView(v) }
        composeView = null
        _isShowing = false
        _isJarvisActive = false
    }

    fun getConfiguration(): JarvisConfig = configuration

    fun hideOverlay() {
        navigator.clear()
        _isShowing = false
    }

    fun activate() {
        if (coreInitialized) _isJarvisActive = true
    }

    fun deactivate() {
        _isJarvisActive = false
        hideOverlay()
    }

    fun isActive(): Boolean = _isJarvisActive

    fun toggle(): Boolean {
        if (_isJarvisActive) deactivate() else activate()
        return _isJarvisActive
    }
}