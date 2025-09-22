@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.navigation.routes

import androidx.annotation.RestrictTo


import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.R
import com.jarvis.core.internal.navigation.TopAppBarType
import kotlinx.serialization.Serializable

object JarvisSDKSettingsGraph {

    @Serializable
    data object JarvisSettings : NavigationRoute {
        override val titleTextId: Int = R.string.core_navigation_settings
        override val shouldShowTopAppBar: Boolean = true
        override val shouldShowBottomBar: Boolean = true
        override val topAppBarType: TopAppBarType = TopAppBarType.MEDIUM
        override val dismissable: Boolean = true
    }
}