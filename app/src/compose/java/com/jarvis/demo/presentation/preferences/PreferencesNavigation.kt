package com.jarvis.demo.presentation.preferences

import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.demo.R
import kotlinx.serialization.Serializable

/**
 * Preferences feature destinations following android-clean-config.yml patterns.
 * Simple destination (no parameters) for the Preferences screen.
 */
object PreferencesGraph {
    @Serializable
    data object Preferences : NavigationRoute {
        override val titleTextId: Int = R.string.preferences
        override val shouldShowTopAppBar: Boolean = true
        override val actionIcon: ImageVector = DSIcons.refresh
        override val actionIconContentDescription: Int = R.string.add_random_preferences
        override val actionKey: String = "preferences_add_preferences"
    }
}