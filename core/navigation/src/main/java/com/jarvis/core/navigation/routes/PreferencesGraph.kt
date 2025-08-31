package com.jarvis.core.navigation.routes

import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.navigation.R
import com.jarvis.core.navigation.TopAppBarType
import kotlinx.serialization.Serializable

object JarvisSDKPreferencesGraph {

    @Serializable
    data object JarvisPreferences : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_preferences
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }
}