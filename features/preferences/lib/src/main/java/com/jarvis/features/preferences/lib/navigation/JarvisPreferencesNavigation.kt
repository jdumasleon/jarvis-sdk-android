package com.jarvis.features.preferences.lib.navigation

import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.core.presentation.navigation.TopAppBarType
import com.jarvis.features.preferences.presentation.R
import kotlinx.serialization.Serializable

object JarvisSDKPreferencesGraph {

    @Serializable
    data object JarvisPreferences : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_preferences
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }
}