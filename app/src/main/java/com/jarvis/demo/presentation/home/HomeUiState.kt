package com.jarvis.demo.presentation.home

import com.jarvis.core.internal.presentation.state.ResourceState

typealias HomeUiState = ResourceState<HomeUiData>

/**
 * UiData containing all screen state for HomeScreen
 */
data class HomeUiData(
    val welcomeMessage: String = "Welcome to Jarvis Demo",
    val description: String = "Your network inspection and debugging companion",
    val version: String = "v1.0.0",
    val shakeInstructions: String = "Shake your device or use the inspector to start monitoring network requests",
    val isJarvisActive: Boolean = false,
    val lastRefreshTime: Long? = null
) {
    companion object {
        val mockHomeUiData = HomeUiData(
            welcomeMessage = "Welcome to Jarvis Demo",
            description = "Your network inspection and debugging companion",
            version = "v1.0.0-beta",
            shakeInstructions = "Shake your device or use the inspector to start monitoring network requests",
            isJarvisActive = true,
            lastRefreshTime = System.currentTimeMillis()
        )
    }
}

/**
 * Events for HomeScreen user interactions
 */
sealed interface HomeEvent {
    object RefreshData : HomeEvent
    object ToggleJarvisMode : HomeEvent
    object ClearError : HomeEvent
}