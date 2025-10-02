package com.jarvis.demo.koin.presentation.inspector

import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.demo.koin.R
import kotlinx.serialization.Serializable

/**
 * Inspector feature destinations following android-clean-config.yml patterns.
 * Simple destination with action icon for network inspection.
 */
object InspectorGraph {
    @Serializable
    data object Inspector : NavigationRoute {
        override val titleTextId: Int = R.string.inspector
        override val shouldShowTopAppBar: Boolean = true
        override val actionIcon: ImageVector = DSIcons.add
        override val actionIconContentDescription: Int = R.string.add_random_request
        override val actionKey: String = "inspector_add_random_api_call"
    }
}