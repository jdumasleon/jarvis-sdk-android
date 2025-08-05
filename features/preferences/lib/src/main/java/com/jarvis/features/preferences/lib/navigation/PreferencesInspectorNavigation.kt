package com.jarvis.features.preferences.lib.navigation

import com.jarvis.core.presentation.navigation.NavigationRoute
import kotlinx.serialization.Serializable

@Serializable
data object PreferencesInspectorRoute : NavigationRoute {
    override val route: String = "preferences_inspector"
    override val shouldShowTopAppBar: Boolean = true
}