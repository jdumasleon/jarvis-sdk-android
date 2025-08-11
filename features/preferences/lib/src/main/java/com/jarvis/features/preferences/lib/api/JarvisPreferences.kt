package com.jarvis.features.preferences.lib.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jarvis.features.preferences.presentation.ui.PreferencesInspectorRoute

/**
 * Main API for Jarvis Preferences Inspector feature.
 * 
 * This class provides access to the preferences inspection functionality
 * that allows developers to view, edit, and manage all application preferences
 * including DataStore preferences and SharedPreferences files.
 */
object JarvisPreferencesInspector {
    
    /**
     * Creates a composable screen that displays all application preferences
     * with search, filtering, and editing capabilities.
     * 
     * Features:
     * - View all preferences from DataStore and SharedPreferences
     * - Search and filter preferences by type and name
     * - Add, edit, and delete individual preferences
     * - Export preferences to JSON format
     * - Import preferences from JSON format
     * - Clear all preferences
     * - Distinguish between user and system preferences
     * 
     * @param onNavigateBack Callback for back navigation
     * @param modifier Modifier to be applied to the screen
     * @return Composable screen for preferences inspection
     */
    @Composable
    fun PreferencesInspectorScreen(
        onNavigateBack: () -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        PreferencesInspectorRoute(
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}