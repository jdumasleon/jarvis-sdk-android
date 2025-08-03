package com.jarvis.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
            selectedTextColor = DSNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = DSNavigationDefaults.navigationUnSelectedItemColor(),
            unselectedTextColor = DSNavigationDefaults.navigationUnSelectedItemColor(),
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
        contentColor = DSNavigationDefaults.navigationContentColor(),
        tonalElevation = DSJarvisTheme.elevations.none,
        content = content,
    )
}

@Preview
@Composable
private fun DSNavigationBarWithoutLabelPreview() {
    val items = listOf("Home", "Map", "Profile", "More")
    val icons = listOf(DSIcons.Home, DSIcons.place, DSIcons.person, DSIcons.moreVert)

    DSJarvisTheme {
        DSNavigationBar {
            items.forEachIndexed { index, item ->
                DSNavigationBarItem(
                    icon = {
                        DSIcon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        DSIcon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selected = index == 0,
                    onClick = { },
                )
            }
        }
    }
}

@Preview
@Composable
fun DSNavigationBarPreview() {
    val items = listOf("Home", "Map", "Profile", "More")
    val icons = listOf(DSIcons.Home, DSIcons.place, DSIcons.person, DSIcons.moreVert)

    DSJarvisTheme {
        DSNavigationBar {
            items.forEachIndexed { index, item ->
                DSNavigationBarItem(
                    icon = {
                        DSIcon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        DSIcon(
                            imageVector = icons[index],
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
    fun navigationContentColor() = DSJarvisTheme.colors.neutral.neutral0

    @Composable
    fun navigationSelectedItemColor() = DSJarvisTheme.colors.primary.primary100

    @Composable
    fun navigationUnSelectedItemColor() = DSJarvisTheme.colors.neutral.neutral40

    @Composable
    fun navigationIndicatorColor() = Color.Transparent
}
