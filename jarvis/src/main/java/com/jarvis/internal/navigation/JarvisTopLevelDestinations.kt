package com.jarvis.internal.navigation

import androidx.annotation.RestrictTo

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.internal.navigation.routes.JarvisSDKHomeGraph
import com.jarvis.library.R
import com.jarvis.features.inspector.R as InspectorR
import com.jarvis.features.preferences.R as PreferencesR
import com.jarvis.core.internal.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKSettingsGraph
import kotlin.reflect.KClass

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
        titleRes = R.string.jarvis_home,
        icon = DSIcons.Outlined.home,
        iconContentDescription = R.string.jarvis_home_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.home
    ),
    INSPECTOR(
        destination = JarvisSDKInspectorGraph.JarvisInspectorTransactions,
        route = JarvisSDKInspectorGraph.JarvisInspectorTransactions::class,
        titleRes = InspectorR.string.jarvis_inspector,
        icon = DSIcons.Outlined.inspector,
        iconContentDescription = InspectorR.string.jarvis_inspector_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.inspector
    ),
    PREFERENCES(
        destination = JarvisSDKPreferencesGraph.JarvisPreferences,
        route = JarvisSDKPreferencesGraph.JarvisPreferences::class,
        titleRes = PreferencesR.string.jarvis_preferences,
        icon = DSIcons.Outlined.preference,
        iconContentDescription = PreferencesR.string.jarvis_preferences_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.preference
    ),
    SETTINGS(
        destination = JarvisSDKSettingsGraph.JarvisSettings,
        route = JarvisSDKSettingsGraph.JarvisSettings::class,
        titleRes = R.string.settings_title,
        icon = DSIcons.Outlined.settings,
        iconContentDescription = R.string.jarvis_settings_bottom_bar_icon_content_description,
        selectedIcon = DSIcons.Filled.settings
    )
}