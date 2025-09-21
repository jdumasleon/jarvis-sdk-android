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
import com.jarvis.core.navigation.JarvisSDKNavigator
import com.jarvis.internal.ui.JarvisSDKApplication
import com.jarvis.internal.ui.JarvisSDKFabTools
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
import com.jarvis.library.R
import com.jarvis.core.platform.JarvisPlatform
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

            _isShowing = false
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
                        if (previousJarvisActiveState) _isJarvisActive = previousJarvisActiveState
                        if (previousShowingState) _isShowing = previousShowingState
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
        _isShowing = false
        _isJarvisActive = false
    }

    fun getConfiguration(): JarvisConfig = configuration

    fun hideOverlay() {
        navigator.clear()
        _isShowing = false
    }

    fun activate() { if (coreInitialized) _isJarvisActive = true }

    fun deactivate() {
        _isJarvisActive = false
        hideOverlay()
    }

    fun isActive(): Boolean = _isJarvisActive

    fun toggle(): Boolean { if (_isJarvisActive) deactivate() else activate(); return _isJarvisActive }

    fun getPlatform(): JarvisPlatform? =
        if (coreInitialized && jarvisPlatform.isInitialized()) jarvisPlatform else null
}