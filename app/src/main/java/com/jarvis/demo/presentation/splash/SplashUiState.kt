package com.jarvis.demo.presentation.splash

import com.jarvis.core.presentation.state.ResourceState

typealias SplashUiState = ResourceState<SplashUiData>

/**
 * UiData containing all screen state for SplashScreen
 */
data class SplashUiData(
    val showSplash: Boolean = true,
    val appName: String = "Jarvis Demo",
    val appVersion: String = "v1.0.0",
    val loadingProgress: Float = 0f,
    val initializationMessage: String = "Initializing..."
) {
    companion object {
        val mockSplashUiData = SplashUiData(
            showSplash = true,
            appName = "Jarvis Demo",
            appVersion = "v1.0.0-beta",
            loadingProgress = 0.75f,
            initializationMessage = "Setting up network monitoring..."
        )
        
        val mockSplashCompletedUiData = SplashUiData(
            showSplash = false,
            loadingProgress = 1f,
            initializationMessage = "Ready!"
        )
    }
}

/**
 * Events for SplashScreen user interactions
 */
sealed interface SplashEvent {
    object StartSplash : SplashEvent
    object CompleteSplash : SplashEvent
    object ClearError : SplashEvent
}