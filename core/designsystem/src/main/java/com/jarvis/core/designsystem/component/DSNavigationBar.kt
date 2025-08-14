package com.jarvis.core.designsystem.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    scaleOnSelected: Boolean = true,
    selectedScale: Float = 1.12f,
    fadeOnChange: Boolean = true,
    fadeDurationMillis: Int = 150,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed = interactionSource.collectIsPressedAsState().value
    val pressScale = animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_item_press_scale"
    ).value

    val selectedScaleValue = animateFloatAsState(
        targetValue = if (scaleOnSelected && selected) selectedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_item_selected_scale"
    ).value

    val combinedScale = selectedScaleValue * pressScale

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(modifier = Modifier.scale(combinedScale)) {
                if (fadeOnChange) {
                    Crossfade(
                        targetState = selected,
                        animationSpec = tween(durationMillis = fadeDurationMillis),
                        label = "nav_item_fade"
                    ) { isSelected ->
                        if (isSelected) selectedIcon() else icon()
                    }
                } else {
                    if (selected) selectedIcon() else icon()
                }
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        interactionSource = interactionSource,
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
    topCornerRadius: Dp = DSJarvisTheme.dimensions.none,
    containerColor: Color = DSJarvisTheme.colors.extra.background,
    contentColor: Color = DSNavigationDefaults.navigationContentColor(),
    tonalElevation: Dp = DSJarvisTheme.elevations.none,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(topStart = topCornerRadius, topEnd = topCornerRadius)

    Surface(
        color = containerColor,
        tonalElevation = tonalElevation,
        shape = shape
    ) {
        NavigationBar(
            modifier = modifier.clip(shape),
            containerColor = Color.Transparent,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            content = content,
        )
    }
}

@Preview
@Composable
private fun DSNavigationBarWithoutLabelPreview() {
    val items = listOf("Home", "Inspector", "Preferences", "More")
    val iconsFilled = listOf(DSIcons.Filled.home, DSIcons.Filled.networkCheck, DSIcons.Filled.settings, DSIcons.Filled.moreVert)
    val icons = listOf(DSIcons.Outlined.home, DSIcons.Outlined.networkCheck, DSIcons.Outlined.settings, DSIcons.Outlined.moreVert)

    DSJarvisTheme {
        DSNavigationBar {
            items.forEachIndexed { index, item ->
                DSNavigationBarItem(
                    icon = { DSIcon(imageVector = icons[index], contentDescription = item) },
                    selectedIcon = { DSIcon(imageVector = iconsFilled[index], contentDescription = item) },
                    selected = index == 0,
                    onClick = { /* noop */ },
                )
            }
        }
    }
}

@Preview
@Composable
fun DSNavigationBarPreview() {
    val items = listOf("Home", "Inspector", "Preferences", "More")
    val iconsFilled = listOf(DSIcons.Filled.home, DSIcons.Filled.networkCheck, DSIcons.Filled.settings, DSIcons.Filled.moreVert)
    val icons = listOf(DSIcons.Outlined.home, DSIcons.Outlined.networkCheck, DSIcons.Outlined.settings, DSIcons.Outlined.moreVert)

    DSJarvisTheme {
        DSNavigationBar {
            items.forEachIndexed { index, item ->
                DSNavigationBarItem(
                    icon = { DSIcon(imageVector = icons[index], contentDescription = item) },
                    selectedIcon = {
                        DSIcon(imageVector = iconsFilled[index], contentDescription = item)
                    },
                    label = { Text(item) },
                    selected = index == 0,
                    onClick = { /* noop */ },
                )
            }
        }
    }
}

object DSNavigationDefaults {
    @Composable fun navigationContentColor() = DSJarvisTheme.colors.extra.background
    @Composable fun navigationSelectedItemColor() = DSJarvisTheme.colors.primary.primary60
    @Composable fun navigationUnSelectedItemColor() = DSJarvisTheme.colors.neutral.neutral60
    @Composable fun navigationIndicatorColor() = Color.Transparent
}