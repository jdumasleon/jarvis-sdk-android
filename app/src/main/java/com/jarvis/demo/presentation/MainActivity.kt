package com.jarvis.demo.presentation

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.demo.presentation.navigation.TopLevelDestination
import com.jarvis.demo.presentation.ui.JarvisDemoApp
import com.jarvis.demo.presentation.ui.rememberJarvisDemoAppState
import com.jarvis.demo.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lightTransparentStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )

        installSplashScreen().setKeepOnScreenCondition {
            !splashViewModel.isSplashShow.value
        }

        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

        setContent {
            val appState = rememberJarvisDemoAppState()

            DSJarvisTheme {
                JarvisDemoApp(
                    appState = appState,
                    startDestination = TopLevelDestination.Home.destination,
                )
            }
        }
    }
}