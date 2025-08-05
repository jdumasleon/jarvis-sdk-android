package com.jarvis.features.preferences.lib.navigation

import com.jarvis.core.presentation.navigation.Navigator

/**
 * Extension functions for navigating to Preferences Inspector screens
 */

/**
 * Navigate to the Preferences Inspector screen
 */
fun Navigator.navigateToPreferencesInspector() {
    goTo(PreferencesInspectorRoute)
}