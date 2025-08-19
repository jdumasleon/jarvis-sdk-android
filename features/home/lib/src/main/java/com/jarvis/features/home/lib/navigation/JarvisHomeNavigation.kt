package com.jarvis.features.home.lib.navigation

import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.core.presentation.navigation.TopAppBarType
import com.jarvis.features.home.presentation.R
import kotlinx.serialization.Serializable

object JarvisSDKHomeGraph {
    @Serializable
    data object JarvisHome : NavigationRoute {
        override val titleTextId: Int = R.string.jarvis_home
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }
}