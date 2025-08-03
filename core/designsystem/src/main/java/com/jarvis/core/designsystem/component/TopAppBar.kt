@file:OptIn(ExperimentalMaterial3Api::class)

package com.jarvis.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onBackClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            titleRes?.let {
                Text(text = stringResource(id = it))
            }
        },
        navigationIcon = {
            navigationIcon?.let {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = it,
                        contentDescription = navigationIconContentDescription,
                        tint = DSJarvisTheme.colors.neutral.neutral100,
                    )
                }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = it,
                        contentDescription = actionIconContentDescription,
                        tint = DSJarvisTheme.colors.neutral.neutral100,
                    )
                }
            }
        },
        colors = colors.copy(
            containerColor = DSJarvisTheme.colors.neutral.neutral0,
            navigationIconContentColor = DSJarvisTheme.colors.neutral.neutral100,
            actionIconContentColor = DSJarvisTheme.colors.neutral.neutral100,
            titleContentColor = DSJarvisTheme.colors.neutral.neutral100,
        ),
        modifier = modifier.testTag("niaTopAppBar"),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun DSTopAppBarPreview() {
    DSJarvisTheme {
        DSTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.search,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = DSIcons.moreVert,
            actionIconContentDescription = "Action icon",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar without icons")
@Composable
private fun DSTopAppBarPreviewWithoutIcons() {
    DSJarvisTheme {
        DSTopAppBar(titleRes = R.string.core_design_system_top_app_bar_title)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar without icons")
@Composable
private fun DSTopAppBarPreviewDefault() {
    DSJarvisTheme {
        DSTopAppBar()
    }
}
