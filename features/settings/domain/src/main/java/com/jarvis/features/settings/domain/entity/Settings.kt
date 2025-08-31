package com.jarvis.features.settings.domain.entity

/**
 * Represents a group of settings items in the Settings screen
 */
data class SettingsGroup(
    val title: String,
    val description: String? = null,
    val items: List<SettingsItem>
)

/**
 * Represents a settings item in the Settings screen
 */
data class SettingsItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val value: String? = null,
    val icon: SettingsIcon = SettingsIcon.INFO,
    val type: SettingsItemType = SettingsItemType.INFO,
    val action: SettingsAction,
    val isEnabled: Boolean = true
)

/**
 * Types of settings items
 */
enum class SettingsItemType {
    ACTION,          // Buttons like delete data, share
    EXTERNAL_LINK,   // Links to external resources
    INFO,            // Display-only information
    TOGGLE,           // Switch/toggle controls
    NAVIGATE         // Navigate to another screen
}

/**
 * Actions that can be performed from settings
 */
sealed class SettingsAction {
    object RateApp : SettingsAction()
    object Version : SettingsAction()
    object NavigateToInspector : SettingsAction()
    object NavigateToPreferences : SettingsAction()
    object NavigateToLogging : SettingsAction()
    data class OpenUrl(val url: String) : SettingsAction()
    data class ShareApp(val url: String) : SettingsAction()
    data class OpenEmail(val email: String, val subject: String) : SettingsAction()
}

/**
 * Icons for settings items
 */
enum class SettingsIcon {
    STAR,
    SHARE,
    INFO,
    LINK,
    EMAIL,
    VERSION,
    TWITTER,
    GITHUB,
    RELEASE_NOTES,
    LOGS,
    INSPECTOR,
    PREFERENCES
}