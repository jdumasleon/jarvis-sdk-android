package com.jarvis.demo.presentation

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.demo.presentation.navigation.TopLevelDestination
import com.jarvis.demo.presentation.ui.JarvisDemoApp
import com.jarvis.demo.presentation.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var splashViewModel: SplashViewModel

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lightTransparentStyle = SystemBarStyle.light(scrim = TRANSPARENT, darkScrim = TRANSPARENT)

        installSplashScreen().setKeepOnScreenCondition {
            !splashViewModel.isSplashShow.value
        }

        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

        // Register this activity for shake detection
        // TODO: ActivityJarvisMode.register(this) - check if needed

        setContent {
            DSJarvisTheme {
                JarvisDemoApp(
                    navigator = navigator,
                    entryProviderBuilders = entryProviderBuilders
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Unregister shake detection
        // TODO: ActivityJarvisMode.unregister(this) - check if needed
    }
}