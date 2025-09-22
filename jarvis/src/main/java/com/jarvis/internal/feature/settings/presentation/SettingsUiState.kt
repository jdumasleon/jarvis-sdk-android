@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.settings.presentation

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.presentation.state.ResourceState
import com.jarvis.internal.feature.settings.domain.entity.AppInfo
import com.jarvis.internal.feature.settings.domain.entity.SettingsAppInfo
import com.jarvis.internal.feature.settings.domain.entity.AppInfoMock.mockAppInfo
import com.jarvis.internal.feature.settings.domain.entity.AppInfoMock.mockSettingsAppInfo
import com.jarvis.internal.feature.settings.domain.entity.SettingsAction
import com.jarvis.internal.feature.settings.domain.entity.SettingsGroup
import com.jarvis.internal.feature.settings.domain.entity.SettingsIcon
import com.jarvis.internal.feature.settings.domain.entity.SettingsItem
import com.jarvis.internal.feature.settings.domain.entity.SettingsItemType

/**
 * UI State for Settings screen following ResourceState pattern
 */
typealias SettingsUiState = ResourceState<SettingsUiData>

/**
 * Data class containing all UI data for Settings screen
 */
data class SettingsUiData(
    val settingsItems: List<SettingsGroup> = emptyList(),
    val appInfo: AppInfo? = null,
    val settingsAppInfo: SettingsAppInfo? = null,
    val showDeleteDialog: Boolean = false,
    val isDeletingData: Boolean = false,
    val deleteResult: String? = null,
    val showRatingDialog: Boolean = false,
    val ratingStars: Int = 0,
    val ratingDescription: String = "",
    val isSubmittingRating: Boolean = false,
    val showCallingAppDetailsDialog: Boolean = false
) {
    companion object {
        val mockSettingsUiData: SettingsUiData
            get() = SettingsUiData(
                settingsItems = listOf(
                    SettingsGroup(
                        title = "App",
                        items = listOf(
                            SettingsItem(
                                id = "calling_app_details",
                                title = mockSettingsAppInfo.hostAppInfo.appName,
                                value = "Version ${mockSettingsAppInfo.hostAppInfo.version}",
                                icon = SettingsIcon.APP,
                                type = SettingsItemType.NAVIGATE,
                                action = SettingsAction.ShowCallingAppDetails
                            )
                        )
                    ),
                    SettingsGroup(
                        title = "Jarvis SDK",
                        items = listOf(
                            SettingsItem(
                                id = "version",
                                title = "Version",
                                value = "${mockAppInfo.version} (${mockAppInfo.buildNumber})",
                                icon = SettingsIcon.VERSION,
                                type = SettingsItemType.INFO,
                                action = SettingsAction.Version
                            ),
                            SettingsItem(
                                id = "docs",
                                title = "Documentation",
                                description = "View complete documentation",
                                icon = SettingsIcon.LINK,
                                type = SettingsItemType.EXTERNAL_LINK,
                                action = SettingsAction.OpenUrl("https://jarvis-sdk.dev/docs")
                            ),
                            SettingsItem(
                                id = "release_notes",
                                title = "Release Notes",
                                description = "What's new in this version",
                                icon = SettingsIcon.RELEASE_NOTES,
                                type = SettingsItemType.EXTERNAL_LINK,
                                action = SettingsAction.OpenUrl("https://jarvis-sdk.dev/releases")
                            ),
                        )
                    ),
                    SettingsGroup(
                        title = "Tools",
                        items = listOf(
                            SettingsItem(
                                id = "inspector",
                                title = "Inspector",
                                description = "Manage network requests",
                                icon = SettingsIcon.INSPECTOR,
                                type = SettingsItemType.NAVIGATE,
                                action = SettingsAction.NavigateToInspector
                            ),
                            SettingsItem(
                                id = "preferences",
                                title = "Preferences",
                                description = "Manage application preferences",
                                icon = SettingsIcon.PREFERENCES,
                                type = SettingsItemType.NAVIGATE,
                                action = SettingsAction.NavigateToPreferences
                            ),
                            SettingsItem(
                                id = "logging",
                                title = "Logging (Coming soon)",
                                description = "Manage applications logs",
                                icon = SettingsIcon.LOGS,
                                type = SettingsItemType.NAVIGATE,
                                action = SettingsAction.NavigateToLogging,
                                isEnabled = false
                            )
                        )
                    ),
                    SettingsGroup(
                        title = "Feedback",
                        items = listOf(
                            SettingsItem(
                                id = "rate_app",
                                title = "Rate Jarvis",
                                description = "Help us improve with your feedback",
                                icon = SettingsIcon.STAR,
                                type = SettingsItemType.ACTION,
                                action = SettingsAction.RateApp
                            ),
                            SettingsItem(
                                id = "share_app",
                                title = "Share Jarvis",
                                description = "Tell others about this tool",
                                icon = SettingsIcon.SHARE,
                                type = SettingsItemType.EXTERNAL_LINK,
                                action = SettingsAction.ShareApp(url = "https://jarvis-sdk.dev")
                            ),
                            SettingsItem(
                                id = "contact",
                                title = "Contact Us",
                                description = "Get support or report issues",
                                icon = SettingsIcon.EMAIL,
                                type = SettingsItemType.ACTION,
                                action = SettingsAction.OpenEmail(
                                    email = "support@jarvis-sdk.dev",
                                    subject = "Jarvis SDK Support"
                                )
                            )
                        )
                    ),
                )
            )
    }

}

/**
 * Events that can be triggered from Settings UI
 */
sealed class SettingsEvent {
    object LoadSettings : SettingsEvent()

    // Rating Events
    object ShowRatingDialog : SettingsEvent()
    object HideRatingDialog : SettingsEvent()
    data class UpdateRatingStars(val stars: Int) : SettingsEvent()
    data class UpdateRatingDescription(val description: String) : SettingsEvent()
    object SubmitRating : SettingsEvent()

    // Calling App Events
    object ShowCallingAppDetailsDialog : SettingsEvent()
    object HideCallingAppDetailsDialog : SettingsEvent()
}