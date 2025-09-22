@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.feature.settings.presentation.components

import androidx.annotation.RestrictTo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.component.DSBottomSheet
import com.jarvis.core.internal.designsystem.component.DSCard
import com.jarvis.core.internal.designsystem.component.DSText
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.internal.feature.settings.domain.entity.HostAppInfo
import com.jarvis.internal.feature.settings.domain.entity.AppInfoMock.mockHostAppInfo
import com.jarvis.library.R

/**
 * Calling app details bottom sheet for displaying host application information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsBottomSheet(
    hostAppInfo: HostAppInfo,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    DSBottomSheet(
        modifier = modifier,
        onDismissRequest = onCancel,
        title = {
            DSText(
                text = stringResource(R.string.features_settings_presentation_app_details),
                style = DSJarvisTheme.typography.title.large,
                fontWeight = FontWeight.Bold
            )
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.xs)
            ) {
                DSCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = DSJarvisTheme.shapes.s,
                    elevation = DSJarvisTheme.elevations.level1
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DSJarvisTheme.spacing.s)
                    ) {
                        AppDetailItem(
                            label = stringResource(R.string.features_settings_presentation_name),
                            value = hostAppInfo.appName
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                            color = DSJarvisTheme.colors.neutral.neutral0
                        )
                        AppDetailItem(
                            label = stringResource(R.string.features_settings_presentation_version),
                            value = hostAppInfo.version
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                            color = DSJarvisTheme.colors.neutral.neutral0
                        )
                        AppDetailItem(
                            label = stringResource(R.string.features_settings_presentation_build_number),
                            value = hostAppInfo.buildNumber
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                            color = DSJarvisTheme.colors.neutral.neutral0
                        )
                        AppDetailItem(
                            label = stringResource(R.string.features_settings_presentation_package_name),
                            value = hostAppInfo.packageName
                        )

                        hostAppInfo.minSdkVersion?.let { minSdk ->
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                color = DSJarvisTheme.colors.neutral.neutral0
                            )
                            AppDetailItem(
                                label = stringResource(R.string.features_settings_presentation_min_sdk_version),
                                value = minSdk.toString()
                            )
                        }

                        hostAppInfo.targetSdkVersion?.let { targetSdk ->
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                color = DSJarvisTheme.colors.neutral.neutral0
                            )
                            AppDetailItem(
                                label = stringResource(R.string.features_settings_presentation_target_sdk_version),
                                value = targetSdk.toString()
                            )
                        }
                    }
                }

                if (hostAppInfo.permissions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.s))

                    DSText(
                        text = stringResource(R.string.features_settings_presentation_permissions).uppercase(),
                        style = DSJarvisTheme.typography.body.medium,
                        color = DSJarvisTheme.colors.neutral.neutral100,
                        modifier = Modifier.padding(start = DSJarvisTheme.spacing.m, bottom = DSJarvisTheme.spacing.s)
                    )

                    DSCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = DSJarvisTheme.shapes.s,
                        elevation = DSJarvisTheme.elevations.level1
                    ) {
                        hostAppInfo.permissions.onEachIndexed { index, permission ->
                            PermissionItem(permission = permission)
                            if (index < hostAppInfo.permissions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.s),
                                    color = DSJarvisTheme.colors.neutral.neutral0
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AppDetailItem(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DSJarvisTheme.spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DSText(
            modifier = Modifier.padding(end = DSJarvisTheme.spacing.s),
            text = label,
            style = DSJarvisTheme.typography.body.medium,
        )
        DSText(
            text = value,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
}

@Composable
private fun PermissionItem(
    permission: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DSJarvisTheme.spacing.s, vertical = DSJarvisTheme.spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DSText(
            text = permission,
            style = DSJarvisTheme.typography.body.medium,
            color = DSJarvisTheme.colors.neutral.neutral100
        )
    }
}

@Preview(showBackground = true, name = "App Details Bottom Sheet")
@Composable
private fun AppDetailsBottomSheetPreview() {
    DSJarvisTheme {
        AppDetailsBottomSheet(
            hostAppInfo = mockHostAppInfo,
            onCancel = {}
        )
    }
}