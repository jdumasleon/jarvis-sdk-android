package com.jarvis.features.settings.domain.usecase

import com.jarvis.features.settings.domain.entity.AppInfo
import com.jarvis.features.settings.domain.entity.SettingsAction
import com.jarvis.features.settings.domain.entity.SettingsGroup
import com.jarvis.features.settings.domain.entity.SettingsIcon
import com.jarvis.features.settings.domain.entity.SettingsItem
import com.jarvis.features.settings.domain.entity.SettingsItemType
import com.jarvis.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject

/**
 * Use case to get all settings items for the Settings screen
 */
class GetSettingsItemsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<Result<List<SettingsGroup>>> = channelFlow {
        settingsRepository.getAppInfo().collect {
            it.fold(
                onFailure = {
                    send(Result.failure(it))
                },
                onSuccess = {
                    val settingsItems = getSettingsItems(it)
                    send(Result.success(settingsItems))
                }
            )
        }

    }


    private fun getSettingsItems(appInfo: AppInfo): List<SettingsGroup> =
        listOf(
            SettingsGroup(
                title = "About",
                items = listOf(
                    SettingsItem(
                        id = "version",
                        title = "Version",
                        value = "${appInfo.version} (${appInfo.buildNumber})",
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
                        action = SettingsAction.OpenUrl("https://jdumasleon.com/work/jarvis")
                    ),
                    SettingsItem(
                        id = "release_notes",
                        title = "Release Notes",
                        description = "What's new in this version",
                        icon = SettingsIcon.RELEASE_NOTES,
                        type = SettingsItemType.EXTERNAL_LINK,
                        action = SettingsAction.OpenUrl("https://jdumasleon.com/work/jarvis")
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
                        title = "Rate Jarvis SDK",
                        description = "Help us improve with your feedback",
                        icon = SettingsIcon.STAR,
                        type = SettingsItemType.ACTION,
                        action = SettingsAction.RateApp
                    ),
                    SettingsItem(
                        id = "share_app",
                        title = "Share Jarvis SDK",
                        description = "Tell others about this tool",
                        icon = SettingsIcon.SHARE,
                        type = SettingsItemType.EXTERNAL_LINK,
                        action = SettingsAction.ShareApp("https://jdumasleon.com/work/jarvis")
                    ),
                    SettingsItem(
                        id = "contact",
                        title = "Contact Us",
                        description = "Get support or report issues",
                        icon = SettingsIcon.EMAIL,
                        type = SettingsItemType.ACTION,
                        action = SettingsAction.OpenEmail(
                            email = "jdumasleon@gmail.com",
                            subject = "Jarvis SDK Support"
                        )
                    )
                )
            )
        )
}