package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

@Composable
fun RowScope.DSNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationContentColor(),
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationContentColor(),
            indicatorColor = DSNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

@Composable
fun DSNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DSNavigationDefaults.navigationContainerColor(),
        contentColor = DSNavigationDefaults.navigationContainerColor(),
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun DSNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationContentColor(),
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationContentColor(),
            indicatorColor = DSNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

@Composable
fun DSNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = DSNavigationDefaults.navigationContainerColor(),
        contentColor = DSNavigationDefaults.navigationContainerColor(),
        header = header,
        content = content,
    )
}

@OptIn(
    ExperimentalMaterial3AdaptiveNavigationSuiteApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
fun DSNavigationSuiteScaffold(
    navigationSuiteItems: DSNavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit,
) {
    val layoutType = NavigationSuiteScaffoldDefaults
        .calculateFromAdaptiveInfo(windowAdaptiveInfo)
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationContentColor(),
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationContentColor(),
            indicatorColor = DSNavigationDefaults.navigationIndicatorColor(),
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationContentColor(),
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationContentColor(),
            indicatorColor = DSNavigationDefaults.navigationIndicatorColor(),
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationContentColor(),
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationContentColor(),
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            DSNavigationSuiteScope(
                navigationSuiteScope = this,
                navigationSuiteItemColors = navigationSuiteItemColors,
            ).run(navigationSuiteItems)
        },
        layoutType = layoutType,
        containerColor = Color.Transparent,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContentColor = DSNavigationDefaults.navigationContentColor(),
            navigationRailContainerColor = Color.Transparent,
        ),
        modifier = modifier,
    ) {
        content()
    }
}

class DSNavigationSuiteScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors,
) {
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: @Composable () -> Unit,
        selectedIcon: @Composable () -> Unit = icon,
        label: @Composable (() -> Unit)? = null,
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        icon = {
            if (selected) {
                selectedIcon()
            } else {
                icon()
            }
        },
        label = label,
        colors = navigationSuiteItemColors,
        modifier = modifier,
    )
}

@Preview
@Composable
fun NiaNavigationBarPreview() {
    val items = listOf("Person", "Search", "Settings")
    val icons = listOf(
        DSIcons.Person,
        DSIcons.Search,
        DSIcons.Settings,
    )
    val selectedIcons = listOf(
        DSIcons.Person,
        DSIcons.Search,
        DSIcons.Settings,
    )

    DSJarvisTheme {
        DSNavigationBar {
            items.forEachIndexed { index, item ->
                DSNavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

@Preview
@Composable
fun NiaNavigationRailPreview() {
    val items = listOf("For you", "Saved", "Interests")
    val icons = listOf(
        DSIcons.Person,
        DSIcons.Search,
        DSIcons.Settings,
    )
    val selectedIcons = listOf(
        DSIcons.Person,
        DSIcons.Search,
        DSIcons.Settings,
    )

    DSJarvisTheme {
        DSNavigationRail {
            items.forEachIndexed { index, item ->
                DSNavigationRailItem(
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

object DSNavigationDefaults {
    @Composable
    fun navigationContainerColor() = DSJarvisTheme.colors.primary.primary20

    @Composable
    fun navigationContentColor() = DSJarvisTheme.colors.neutral.neutral80

    @Composable
    fun navigationSelectedItemColor() = DSJarvisTheme.colors.primary.primary100

    @Composable
    fun navigationIndicatorColor() = DSJarvisTheme.colors.primary.primary20
}

