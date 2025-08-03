package com.jarvis.demo.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.demo.R
import com.jarvis.demo.presentation.home.HomeDestinations
import com.jarvis.demo.presentation.inspector.InspectorDestinations
import com.jarvis.demo.presentation.preferences.PreferencesDestinations
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val destination: NavigationRoute,
    val route: KClass<*>,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    HOME(
        destination = HomeDestinations.Home,
        route = HomeDestinations.Home::class,
        titleRes = R.string.home,
        icon = DSIcons.Home,
        selectedIcon = DSIcons.HomeFilled
    ),
    INSPECTOR(
        destination = InspectorDestinations.Inspector,
        route = InspectorDestinations.Inspector::class,
        titleRes = R.string.inspector,
        icon = DSIcons.NetworkWifi,
        selectedIcon = DSIcons.NetworkWifi
    ),
    PREFERENCES(
        destination = PreferencesDestinations.Preferences,
        route = PreferencesDestinations.Preferences::class,
        titleRes = R.string.preferences,
        icon = DSIcons.Settings,
        selectedIcon = DSIcons.SettingsFilled
    ),
}