package com.jarvis.demo.presentation

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.jarvis.api.core.JarvisSDK
import com.jarvis.api.core.JarvisConfiguration
import com.jarvis.api.core.JarvisProvider
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.core.presentation.state.ResourceState
import com.jarvis.demo.presentation.splash.SplashEvent
import com.jarvis.demo.presentation.splash.SplashScreen
import com.jarvis.demo.presentation.splash.SplashViewModel
import com.jarvis.demo.presentation.ui.JarvisDemoApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>
    
    @Inject
    lateinit var jarvisSDK: JarvisSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lightTransparentStyle = SystemBarStyle.light(scrim = TRANSPARENT, darkScrim = TRANSPARENT)

        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

        // Initialize Jarvis SDK
        jarvisSDK.initialize(
            application = application,
            config = JarvisConfiguration.development()
        )

        setContent {
            DSJarvisTheme {
                JarvisProvider(
                    sdk = jarvisSDK
                ) {
                    val splashViewModel = ViewModelProvider(this@MainActivity)[SplashViewModel::class.java]
                    val uiState by splashViewModel.uiState.collectAsState()
                    
                    // Determine if splash should be shown based on UI state
                    val showSplash = when (val state = uiState) {
                        is ResourceState.Success -> state.data.showSplash
                        is ResourceState.Loading -> true
                        is ResourceState.Idle -> true
                        is ResourceState.Error -> false
                    }
                    
                    // Show splash screen with fade transition
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        SplashScreen(
                            onSplashFinished = { splashViewModel.onEvent(SplashEvent.CompleteSplash) }
                        )
                    }
                    
                    // Show main app with fade transition
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        JarvisDemoApp(
                            navigator = navigator,
                            entryProviderBuilders = entryProviderBuilders
                        )
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister shake detection
        // TODO: ActivityJarvisMode.unregister(this) - check if needed
    }
}