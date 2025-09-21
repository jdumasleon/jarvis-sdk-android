package com.jarvis.core.navigation.routes

import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.R
import com.jarvis.core.navigation.TopAppBarType
import kotlinx.serialization.Serializable

object JarvisSDKHomeGraph {
    @Serializable
    data object JarvisHome : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_home
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }
}