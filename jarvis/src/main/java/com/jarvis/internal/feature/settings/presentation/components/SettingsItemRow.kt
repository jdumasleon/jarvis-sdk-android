@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.settings.presentation.components

import androidx.annotation.RestrictTo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSIcon
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.component.rememberJarvisPrimaryGradient
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.settings.domain.entity.SettingsAction
import com.jarvis.internal.feature.settings.domain.entity.SettingsGroup
import com.jarvis.internal.feature.settings.domain.entity.SettingsIcon
import com.jarvis.internal.feature.settings.domain.entity.SettingsItem
import com.jarvis.internal.feature.settings.domain.entity.SettingsItemType
import com.jarvis.internal.feature.settings.presentation.SettingsUiData.Companion.mockSettingsUiData

@Composable
fun SettingsGroup(
    group: SettingsGroup,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        DSText(
            text = group.title.uppercase(),
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100,
            modifier = Modifier.padding(start = DSJarvisTheme.spacing.m, bottom = DSJarvisTheme.spacing.s)
        )

        DSCard(
            shape = DSJarvisTheme.shapes.s,
            elevation = DSJarvisTheme.elevations.level1,
        ) {
            group.items.forEach { item ->
                SettingsItemRow(
                    item = item,
                    onClick = { onAction(item.action) }
                )

                if (item != group.items.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.xs),
                        color = DSJarvisTheme.colors.neutral.neutral0
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.isEnabled,
                indication = null,
                interactionSource = interactionSource
            ) {
                onClick()
            }
            .padding(
                horizontal = DSJarvisTheme.spacing.s,
                vertical = DSJarvisTheme.spacing.xs
            ),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            DSIcon(
                size = DSJarvisTheme.dimensions.l,
                imageVector = item.icon.toImageVector(),
                contentDescription = item.title,
                tint = when {
                    !item.isEnabled -> DSIconTint.Solid(DSJarvisTheme.colors.neutral.neutral40)
                    item.isEnabled && isJarvisTool(item) -> DSIconTint.Gradient(rememberJarvisPrimaryGradient())
                    else -> DSIconTint.Solid(item.icon.toTintColor())
                }
            )

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                DSText(
                    text = item.title,
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        !item.isEnabled -> DSJarvisTheme.colors.neutral.neutral40
                        else -> DSJarvisTheme.colors.extra.onSurface
                    }
                )

                item.description?.let {
                    DSText(
                        text = item.description,
                        style = DSJarvisTheme.typography.body.small,
                        color = when {
                            !item.isEnabled -> DSJarvisTheme.colors.neutral.neutral40
                            else -> DSJarvisTheme.colors.neutral.neutral80
                        }
                    )
                }
            }

            item.value?.let {
                DSText(
                    text = it,
                    style = DSJarvisTheme.typography.body.medium,
                    color = DSJarvisTheme.colors.neutral.neutral100
                )
            }

            // Trailing icon for external links
            if (item.isEnabled && (item.type == SettingsItemType.EXTERNAL_LINK || item.type == SettingsItemType.NAVIGATE)) {
                DSIcon(
                    imageVector = DSIcons.arrowForwards,
                    contentDescription = "External link",
                    tint = if (isJarvisTool(item)) {
                        DSIconTint.Gradient(rememberJarvisPrimaryGradient())
                    } else {
                        DSIconTint.Solid(item.icon.toTintColor())
                    },
                    size = DSJarvisTheme.dimensions.m
                )
            }
        }
    }
}

private fun isJarvisTool(item: SettingsItem): Boolean {
    return when (item.icon) {
        SettingsIcon.INSPECTOR, SettingsIcon.PREFERENCES, SettingsIcon.LOGS -> true
        else -> false
    }
}

/**
 * Maps SettingsIcon enum to actual ImageVector
 */
private fun SettingsIcon.toImageVector(): ImageVector = when (this) {
    SettingsIcon.INSPECTOR -> DSIcons.networkCheck
    SettingsIcon.PREFERENCES -> DSIcons.Filled.preference
    SettingsIcon.LOGS -> DSIcons.monitor
    SettingsIcon.STAR -> DSIcons.stars
    SettingsIcon.SHARE -> DSIcons.share
    SettingsIcon.INFO -> DSIcons.info
    SettingsIcon.LINK -> DSIcons.link
    SettingsIcon.EMAIL -> DSIcons.email
    SettingsIcon.VERSION -> DSIcons.info
    SettingsIcon.TWITTER -> DSIcons.link
    SettingsIcon.GITHUB -> DSIcons.link
    SettingsIcon.RELEASE_NOTES -> DSIcons.description
    SettingsIcon.APP -> DSIcons.android
}

@Composable
private fun SettingsIcon.toTintColor(): Color = when (this) {
    SettingsIcon.INSPECTOR,
    SettingsIcon.PREFERENCES,
    SettingsIcon.LOGS,
    SettingsIcon.SHARE,
    SettingsIcon.INFO,
    SettingsIcon.LINK,
    SettingsIcon.EMAIL,
    SettingsIcon.VERSION,
    SettingsIcon.TWITTER,
    SettingsIcon.GITHUB,
    SettingsIcon.RELEASE_NOTES -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.APP,
    SettingsIcon.STAR-> DSJarvisTheme.colors.success.success100
}

@Preview(showBackground = true, name = "Settings Item - Action")
@Composable
private fun SettingsItemActionPreview() {
    DSJarvisTheme {
        SettingsItemRow(
            item = SettingsItem(
                id = "delete_data",
                title = "Delete All Data",
                description = "Remove all network logs and preferences",
                icon = SettingsIcon.RELEASE_NOTES,
                type = SettingsItemType.ACTION,
                action = SettingsAction.RateApp
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Item - External Link")
@Composable
private fun SettingsItemExternalPreview() {
    DSJarvisTheme {
        SettingsItemRow(
            item = SettingsItem(
                id = "twitter",
                title = "Follow on Twitter",
                description = "@JarvisSDK",
                icon = SettingsIcon.TWITTER,
                type = SettingsItemType.EXTERNAL_LINK,
                action = SettingsAction.OpenUrl("https://twitter.com")
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Item - Disabled")
@Composable
private fun SettingsItemDisabledPreview() {
    DSJarvisTheme {
        SettingsItemRow(
            item = SettingsItem(
                id = "disabled",
                title = "Disabled Option",
                description = "This option is not available",
                icon = SettingsIcon.INFO,
                type = SettingsItemType.ACTION,
                action = SettingsAction.Version,
                isEnabled = false
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Group")
@Composable
private fun SettingsGroupPreview() {
    DSJarvisTheme {
        SettingsGroup(
            group = mockSettingsUiData.settingsItems.first(),
            onAction = {}
        )
    }
}