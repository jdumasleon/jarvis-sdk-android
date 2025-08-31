package com.jarvis.features.settings.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.component.DSCard
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.features.settings.domain.entity.SettingsAction
import com.jarvis.features.settings.domain.entity.SettingsGroup
import com.jarvis.features.settings.domain.entity.SettingsIcon
import com.jarvis.features.settings.domain.entity.SettingsItem
import com.jarvis.features.settings.domain.entity.SettingsItemType
import com.jarvis.features.settings.presentation.ui.SettingsUiData.Companion.mockSettingsUiData

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
    val colors = listOf(
        DSJarvisTheme.colors.extra.jarvisPink,
        DSJarvisTheme.colors.extra.jarvisBlue
    )
    val brush = remember { Brush.linearGradient(colors) }
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
                vertical = DSJarvisTheme.spacing.s
            ),
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            DSIcon(
                modifier = when {
                     item.isEnabled && isJarvisTool(item) ->
                        Modifier.graphicsLayer(alpha = 0.99f)
                            .size(DSJarvisTheme.dimensions.l)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        brush = brush,
                                        blendMode = BlendMode.SrcIn
                                    )
                                }
                            }
                    else -> Modifier.size(DSJarvisTheme.dimensions.l)
                },
                imageVector = item.icon.toImageVector(),
                contentDescription = item.title,
                tint = when {
                    !item.isEnabled -> DSJarvisTheme.colors.neutral.neutral40
                    else -> item.icon.toTintColor()
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
                    tint = item.icon.toTintColor(),
                    modifier = when {
                        isJarvisTool(item) ->
                            Modifier.graphicsLayer(alpha = 0.99f)
                                .size(DSJarvisTheme.dimensions.m)
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = brush,
                                            blendMode = BlendMode.SrcIn
                                        )
                                    }
                                }
                        else -> Modifier.size(DSJarvisTheme.dimensions.m)
                    },
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
}

@Composable
private fun SettingsIcon.toTintColor(): Color = when (this) {
    SettingsIcon.INSPECTOR -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.PREFERENCES -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.LOGS -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.STAR -> DSJarvisTheme.colors.success.success100
    SettingsIcon.SHARE -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.INFO -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.LINK -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.EMAIL -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.VERSION -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.TWITTER -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.GITHUB -> DSJarvisTheme.colors.primary.primary60
    SettingsIcon.RELEASE_NOTES -> DSJarvisTheme.colors.primary.primary60
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