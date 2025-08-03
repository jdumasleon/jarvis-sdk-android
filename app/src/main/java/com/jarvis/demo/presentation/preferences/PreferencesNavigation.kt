package com.jarvis.demo.presentation.preferences

import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.demo.R
import kotlinx.serialization.Serializable

/**
 * Preferences feature destinations following android-clean-config.yml patterns.
 * Simple destination (no parameters) for the Preferences screen.
 */
object PreferencesDestinations {
    @Serializable
    data object Preferences : NavigationRoute {
        override val titleTextId: Int = R.string.preferences
        override val shouldShowTopAppBar: Boolean = true
    }
}