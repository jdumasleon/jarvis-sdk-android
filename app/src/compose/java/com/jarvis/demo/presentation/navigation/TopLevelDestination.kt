package com.jarvis.demo.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.demo.R
import com.jarvis.demo.presentation.home.HomeGraph
import com.jarvis.demo.presentation.inspector.InspectorGraph
import com.jarvis.demo.presentation.preferences.PreferencesGraph
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val destination: NavigationRoute,
    val route: KClass<*>,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    HOME(
        destination = HomeGraph.Home,
        route = HomeGraph.Home::class,
        titleRes = R.string.home,
        icon = DSIcons.home,
        selectedIcon = DSIcons.homeFilled
    ),
    INSPECTOR(
        destination = InspectorGraph.Inspector,
        route = InspectorGraph.Inspector::class,
        titleRes = R.string.inspector,
        icon = DSIcons.networkWifi,
        selectedIcon = DSIcons.networkWifi
    ),
    PREFERENCES(
        destination = PreferencesGraph.Preferences,
        route = PreferencesGraph.Preferences::class,
        titleRes = R.string.preferences,
        icon = DSIcons.settings,
        selectedIcon = DSIcons.settingsFilled
    )
}