package com.jarvis.demo.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.navigation.Destination
import com.jarvis.core.navigation.createRoutePattern
import com.jarvis.demo.R
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composable.
 */
@OptIn(ExperimentalSerializationApi::class)
sealed interface TopLevelDestination: Destination {
    companion object {
        val entries: List<TopLevelDestination> = listOf(
            Home
        )
    }

    @Serializable
    data object Home : TopLevelDestination {
        @StringRes
        override val titleTextId: Int = R.string.app_name
        override val route: String = createRoutePattern<Home>()
        override val destination: Destination = Home
    }
}
