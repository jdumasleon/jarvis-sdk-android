package com.jarvis.api.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import com.jarvis.features.home.presentation.R as HomeR
import com.jarvis.features.inspector.presentation.R as InspectorR
import com.jarvis.features.preferences.presentation.R as PreferencesR
import com.jarvis.features.inspector.lib.navigation.JarvisSDKInspectorGraph
import com.jarvis.features.preferences.lib.navigation.JarvisSDKPreferencesGraph
import kotlin.reflect.KClass

enum class JarvisTopLevelDestinations(
    val destination: NavigationRoute,
    val route: KClass<*>,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val iconContentDescription: Int? = null,
    val selectedIcon: ImageVector = icon,
    val selectedIconContentDescription: Int? = iconContentDescription
) {
    HOME(
        destination = JarvisSDKHomeGraph.JarvisHome,
        route = JarvisSDKHomeGraph.JarvisHome::class,
        titleRes = HomeR.string.jarvis_home,
        icon = DSIcons.Outlined.home,
        iconContentDescription = HomeR.string.jarvis_home_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.home
    ),
    INSPECTOR(
        destination = JarvisSDKInspectorGraph.JarvisInspectorTransactions,
        route = JarvisSDKInspectorGraph.JarvisInspectorTransactions::class,
        titleRes = InspectorR.string.jarvis_inspector,
        icon = DSIcons.Outlined.networkCheck,
        iconContentDescription = InspectorR.string.jarvis_inspector_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.networkCheck
    ),
    PREFERENCES(
        destination = JarvisSDKPreferencesGraph.JarvisPreferences,
        route = JarvisSDKPreferencesGraph.JarvisPreferences::class,
        titleRes = PreferencesR.string.jarvis_preferences,
        icon = DSIcons.Outlined.settings,
        iconContentDescription = PreferencesR.string.jarvis_preferences_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.settings

    )
}