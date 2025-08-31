package com.jarvis.demo.presentation.home

import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.demo.R
import kotlinx.serialization.Serializable

/**
 * Home feature destinations following android-clean-config.yml patterns.
 * Simple destination (no parameters) for the Home screen.
 */
object HomeGraph {
    @Serializable
    data object Home : NavigationRoute {
        override val titleTextId: Int = R.string.home
        override val shouldShowTopAppBar: Boolean = true
        override val actionIcon: ImageVector? = DSIcons.refresh
        override val actionIconContentDescription: Int? = R.string.refresh_home
        override val actionKey: String = "home_refresh_action"
    }
}