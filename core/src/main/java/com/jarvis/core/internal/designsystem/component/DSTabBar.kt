@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme

/**
 * Generic DSJarvis TabBar component with indicator
 *
 * @param selectedTabIndex Currently selected tab index
 * @param tabCount Total number of tabs
 * @param onTabSelected Called when a tab is selected
 * @param modifier Optional modifier for TabRow
 * @param backgroundColor Background color of the TabRow
 * @param indicatorColor Color of the indicator line
 * @param tabContent Composable that defines the content for each tab
 */
@Composable
fun DSTabBar(
    selectedTabIndex: Int,
    tabCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DSJarvisTheme.colors.neutral.neutral20,
    indicatorColor: Color = DSJarvisTheme.colors.primary.primary100,
    tabContent: @Composable (index: Int, selected: Boolean) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = backgroundColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                height = DSJarvisTheme.dimensions.xxs,
                color = indicatorColor,
            )
        },
        divider = {}
    ) {
        repeat(tabCount) { index ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = DSJarvisTheme.colors.primary.primary100,
                unselectedContentColor = DSJarvisTheme.colors.neutral.neutral100
            ) {
                tabContent(index, selectedTabIndex == index)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DSTabBarWithIndicatorPreview() {
    val tabs = listOf("SharedPrefs", "DataStore", "Proto")
    DSJarvisTheme {
        DSTabBar(
            selectedTabIndex = 1,
            tabCount = tabs.size,
            onTabSelected = {}
        ) { index, selected ->
            Box (
                modifier = Modifier.padding(DSJarvisTheme.spacing.m)
            ) {
                DSText(
                    text = tabs[index],
                    style = DSJarvisTheme.typography.body.medium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) {
                        DSJarvisTheme.colors.primary.primary100
                    } else {
                        DSJarvisTheme.colors.neutral.neutral100
                    }
                )
            }
        }
    }
}