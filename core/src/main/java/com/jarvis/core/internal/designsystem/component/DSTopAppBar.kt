@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@file:OptIn(ExperimentalMaterial3Api::class)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.R
import com.jarvis.core.internal.designsystem.icons.DSIcons
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    logo: (@Composable () -> Unit)? = null,
    dismissable: Boolean = false,
    scrollUpBackgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    defaultBackgroundColor: Color = DSJarvisTheme.colors.extra.background,
    enableScrollColorChange: Boolean = false,
    isScrolledUp: Boolean = false,
    onBackClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val backgroundColorProgress by animateFloatAsState(
        targetValue = if (isScrolledUp && enableScrollColorChange) 1f else 0f,
        animationSpec = tween(300),
        label = "topAppBarBackgroundColor"
    )
    
    val currentBackgroundColor = lerp(defaultBackgroundColor, scrollUpBackgroundColor, backgroundColorProgress)
    CenterAlignedTopAppBar(
            title = {
                titleRes?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (logo != null && navigationIcon == null) {
                            Spacer(Modifier.width(DSJarvisTheme.spacing.xs))
                            Box(Modifier.size(DSJarvisTheme.dimensions.xl)) { logo() }
                            Spacer(Modifier.width(DSJarvisTheme.spacing.xs))
                        }
                        DSText(
                            modifier = Modifier
                                .padding(
                                    start = if (logo != null) DSJarvisTheme.spacing.none else DSJarvisTheme.spacing.xxxl,
                                    end = if (actionIcon != null && !dismissable) {
                                        DSJarvisTheme.spacing.xxxl
                                    } else if (actionIcon == null && !dismissable) {
                                        DSJarvisTheme.spacing.xxxxl
                                    } else {
                                        DSJarvisTheme.spacing.none
                                    }
                                )
                                .weight(1f),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = it),
                            style = DSJarvisTheme.typography.title.large,
                            color = DSJarvisTheme.colors.extra.black
                        )
                    }
                }
            },
            navigationIcon = {
                navigationIcon?.let {
                    IconButton(onClick = onBackClick) {
                        DSIcon(
                            imageVector = it,
                            contentDescription = navigationIconContentDescription,
                            tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                        )
                    }

                }
            },
            actions = {
                actionIcon?.let {
                    IconButton(onClick = onActionClick) {
                        DSIcon(
                            imageVector = it,
                            contentDescription = actionIconContentDescription,
                            tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                        )
                    }
                }
                if (dismissable) {
                    IconButton(onClick = onDismiss) {
                        DSIcon(
                            imageVector = DSIcons.Rounded.close,
                            contentDescription = "Close",
                            tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = currentBackgroundColor,
                navigationIconContentColor = DSJarvisTheme.colors.extra.black,
                actionIconContentColor = DSJarvisTheme.colors.extra.black,
                titleContentColor = DSJarvisTheme.colors.extra.black,
            ),
            modifier = modifier.testTag("niaTopAppBar")
        )
}

@Composable
fun DSMediumTopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    dismissable: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    logo: (@Composable () -> Unit)? = null,
    expandedLogoSize: Dp = DSJarvisTheme.dimensions.xxl,
    collapsedLogoSize: Dp = DSJarvisTheme.dimensions.l,
    logoSpacing: Dp = DSJarvisTheme.spacing.m,
    collapseThreshold: Float = 0.5f,
    scrollUpBackgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    defaultBackgroundColor: Color = DSJarvisTheme.colors.extra.background,
    enableScrollColorChange: Boolean = false,
    isScrolledUp: Boolean = false,
    onBackClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 1f
    val isCollapsed = collapsedFraction >= collapseThreshold
    val titleText = titleRes?.let { stringResource(id = it) }
    
    val backgroundColorProgress by animateFloatAsState(
        targetValue = if (isScrolledUp && enableScrollColorChange) 1f else 0f,
        animationSpec = tween(300),
        label = "mediumTopAppBarBackgroundColor"
    )
    
    val currentBackgroundColor = lerp(defaultBackgroundColor, scrollUpBackgroundColor, backgroundColorProgress)

    MediumTopAppBar(
        title = {
            if (titleText != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    DSText(
                        text = titleText,
                        style = DSJarvisTheme.typography.title.large,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = DSJarvisTheme.colors.extra.black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                navigationIcon?.let {
                    IconButton(onClick = onBackClick) {
                        DSIcon(
                            imageVector = it,
                            contentDescription = navigationIconContentDescription,
                            tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                        )
                    }
                }
                // When expanded → show logo to the right of back button
                if (logo != null && !isCollapsed) {
                    Spacer(Modifier.width(logoSpacing))
                    Box(Modifier.size(expandedLogoSize)) { logo() }
                }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionClick) {
                    DSIcon(
                        imageVector = it,
                        contentDescription = actionIconContentDescription,
                        tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                    )
                }
            }
            if (dismissable) {
                IconButton(onClick = onDismiss) {
                    DSIcon(
                        imageVector = DSIcons.Rounded.close,
                        contentDescription = "Close",
                        tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = currentBackgroundColor,
            scrolledContainerColor = currentBackgroundColor,
            navigationIconContentColor = DSJarvisTheme.colors.extra.black,
            actionIconContentColor = DSJarvisTheme.colors.extra.black,
            titleContentColor = DSJarvisTheme.colors.extra.black,
        ),
        modifier = modifier.testTag("niaMediumTopAppBar"),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun DSLargeTopAppBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int? = null,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    dismissable: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    logo: (@Composable () -> Unit)? = null,
    expandedLogoSize: Dp = DSJarvisTheme.dimensions.xxl,
    collapsedLogoSize: Dp = DSJarvisTheme.dimensions.l,
    logoSpacing: Dp = DSJarvisTheme.spacing.m,
    collapseThreshold: Float = 0.5f,
    scrollUpBackgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    defaultBackgroundColor: Color = DSJarvisTheme.colors.extra.background,
    enableScrollColorChange: Boolean = false,
    isScrolledUp: Boolean = false,
    onBackClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 1f
    val isCollapsed = collapsedFraction >= collapseThreshold
    val titleText = titleRes?.let { stringResource(id = it) }
    
    val backgroundColorProgress by animateFloatAsState(
        targetValue = if (isScrolledUp && enableScrollColorChange) 1f else 0f,
        animationSpec = tween(300),
        label = "largeTopAppBarBackgroundColor"
    )
    
    val currentBackgroundColor = lerp(defaultBackgroundColor, scrollUpBackgroundColor, backgroundColorProgress)

    LargeTopAppBar(
        title = {
            if (titleText != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DSText(
                        text = titleText,
                        style = DSJarvisTheme.typography.heading.large,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = DSJarvisTheme.colors.extra.black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                navigationIcon?.let {
                    IconButton(onClick = onBackClick) {
                        DSIcon(
                            imageVector = it,
                            contentDescription = navigationIconContentDescription,
                            tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                        )
                    }
                }
                if (logo != null && !isCollapsed) {
                    Spacer(Modifier.width(logoSpacing))
                    Box(Modifier.size(expandedLogoSize)) { logo() }
                }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionClick) {
                    DSIcon(
                        imageVector = it,
                        contentDescription = actionIconContentDescription,
                        tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                    )
                }
            }
            if (dismissable) {
                IconButton(onClick = onDismiss) {
                    DSIcon(
                        imageVector = DSIcons.Rounded.close,
                        contentDescription = "Close",
                        tint = DSIconTint.Solid(DSJarvisTheme.colors.extra.black),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = currentBackgroundColor,
            scrolledContainerColor = currentBackgroundColor,
            navigationIconContentColor = DSJarvisTheme.colors.extra.black,
            actionIconContentColor = DSJarvisTheme.colors.extra.black,
            titleContentColor = DSJarvisTheme.colors.extra.black,
        ),
        modifier = modifier.testTag("niaLargeTopAppBar"),
        scrollBehavior = scrollBehavior
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
@Preview("Top App Bar")
@Composable
private fun DSTopAppBarDismissablePreview() {
    DSJarvisTheme {
        DSTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.search,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = DSIcons.moreVert,
            logo = {
                DSIcon(
                    painter = painterResource(R.drawable.ic_jarvis_logo),
                    contentDescription = "Jarvis Logo"
                )
            },
            actionIconContentDescription = "Action icon",
            dismissable = true,
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun DSTopAppBaPreview() {
    DSJarvisTheme {
        DSTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.search,
            navigationIconContentDescription = "Navigation icon",
            logo = {
                DSIcon(
                    painter = painterResource(R.drawable.ic_jarvis_logo),
                    contentDescription = "Jarvis Logo"
                )
            },
            actionIconContentDescription = "Action icon",
            onDismiss = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar without icons")
@Composable
private fun DSTopAppBarPreviewWithoutIcons() {
    DSJarvisTheme {
        DSTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            logo = {
                DSIcon(
                    modifier = Modifier.size(DSJarvisTheme.dimensions.xl),
                    painter = painterResource(R.drawable.ic_jarvis_logo),
                    contentDescription = "Jarvis Logo"
                )
            },
            dismissable = true
        )
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


@Preview("Medium Top App Bar")
@Composable
private fun DSMediumTopAppBarPreview() {
    DSJarvisTheme {
        DSMediumTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.arrowBack,
            navigationIconContentDescription = "Back",
            actionIcon = DSIcons.moreVert,
            actionIconContentDescription = "Menu",
        )
    }
}

@Preview("Medium Top App Bar - Dismissable")
@Composable
private fun DSMediumTopAppBarDismissablePreview() {
    DSJarvisTheme {
        DSMediumTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.arrowBack,
            navigationIconContentDescription = "Back",
            actionIcon = DSIcons.moreVert,
            actionIconContentDescription = "Menu",
            logo = {
                DSIcon(
                    painter = painterResource(R.drawable.ic_jarvis_logo),
                    contentDescription = "Jarvis Logo"
                )
            },
            dismissable = true,
            onDismiss = {}
        )
    }
}

@Preview("Medium Top App Bar - No icons")
@Composable
private fun DSMediumTopAppBarNoIconsPreview() {
    DSJarvisTheme {
        DSMediumTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title
        )
    }
}

@Preview("Large Top App Bar")
@Composable
private fun DSLargeTopAppBarPreview() {
    DSJarvisTheme {
        DSLargeTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.arrowBack,
            navigationIconContentDescription = "Back",
            actionIcon = DSIcons.moreVert,
            actionIconContentDescription = "Menu",
        )
    }
}

@Preview("Large Top App Bar - Dismissable")
@Composable
private fun DSLargeTopAppBarDismissablePreview() {
    DSJarvisTheme {
        DSLargeTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title,
            navigationIcon = DSIcons.Rounded.arrowBack,
            navigationIconContentDescription = "Back",
            actionIcon = DSIcons.moreVert,
            actionIconContentDescription = "Menu",
            dismissable = true,
            onDismiss = {}
        )
    }
}

@Preview("Large Top App Bar - No icons")
@Composable
private fun DSLargeTopAppBarNoIconsPreview() {
    DSJarvisTheme {
        DSLargeTopAppBar(
            titleRes = R.string.core_design_system_top_app_bar_title
        )
    }
}