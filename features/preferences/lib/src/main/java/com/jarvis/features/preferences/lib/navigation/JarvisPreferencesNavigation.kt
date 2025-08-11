package com.jarvis.features.preferences.lib.navigation

import android.graphics.pdf.content.PdfPageGotoLinkContent
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.features.preferences.presentation.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface PreferencesGraph : NavigationRoute {

    @Serializable
    data object Preferences : PreferencesGraph {
        override val shouldShowTopAppBar: Boolean = true
        override val titleTextId: Int = R.string.preferences_inspector_title
    }
}

fun NavController.navigateToHome(navOptions: NavOptions) = navigate(route = HomeGraph.Home, navOptions)

fun NavGraphBuilder.homeScreen(onCurrentDestinationChanged: (PdfPageGotoLinkContent.Destination) -> Unit) {
    composable<PreferencesGraph.Preferences> {
        onCurrentDestinationChanged(PreferencesGraph.Preferences)
        PreferencesRoute()
    }
}