package com.jarvis.demo.presentation.home

import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.demo.R
import kotlinx.serialization.Serializable

/**
 * Home feature destinations following android-clean-config.yml patterns.
 * Simple destination (no parameters) for the Home screen.
 */
object HomeDestinations {
    @Serializable
    data object Home : NavigationRoute {
        override val titleTextId: Int = R.string.home
        override val shouldShowTopAppBar: Boolean = true
    }
}