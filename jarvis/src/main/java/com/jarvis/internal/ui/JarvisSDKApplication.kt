@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.ui

import androidx.annotation.RestrictTo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jarvis.internal.navigation.JarvisSDKNavDisplay
import com.jarvis.internal.navigation.JarvisTopLevelDestinations
import com.jarvis.core.R
import com.jarvis.core.internal.designsystem.component.DSIcon
import com.jarvis.core.internal.designsystem.component.DSIconTint
import com.jarvis.core.internal.designsystem.component.DSLargeTopAppBar
import com.jarvis.core.internal.designsystem.component.rememberJarvisPrimaryGradient
import com.jarvis.core.internal.designsystem.component.DSMediumTopAppBar
import com.jarvis.core.internal.designsystem.component.DSNavigationBar
import com.jarvis.core.internal.designsystem.component.DSNavigationBarItem
import com.jarvis.core.internal.designsystem.component.DSTopAppBar
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.navigation.ActionRegistry
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.navigation.TopAppBarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSDKApplication(
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    onDismiss: () -> Unit = {}
) {
    // Reactive state that updates when navigator changes
    var currentDestination by remember { mutableStateOf(navigator.currentDestination) }
    
    // Update current destination when navigator back stack changes
    LaunchedEffect(navigator.backStack.size) {
        currentDestination = navigator.currentDestination
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = DSJarvisTheme.colors.extra.background,
        topBar = {
            JarvisSDKTopBar(
                currentDestination = currentDestination,
                navigator = navigator,
                scrollBehavior = scrollBehavior,
                onDismiss = onDismiss,
            )
        },
        bottomBar = {
            JarvisSDKBottomBar(
                currentDestination = currentDestination,
                navigator = navigator
            )
        }
    ) { padding ->
        JarvisSDKNavDisplay(
            navigator = navigator,
            entryProviderBuilders = entryProviderBuilders,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding() - DSJarvisTheme.spacing.m,
                    bottom = padding.calculateBottomPadding() - DSJarvisTheme.spacing.m
                ),
            onCurrentDestinationChanged = { currentDestination = it }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun JarvisSDKTopBar(
    currentDestination: NavigationRoute?,
    navigator: Navigator,
    scrollBehavior: TopAppBarScrollBehavior,
    onDismiss: () -> Unit = {}
) {
    currentDestination?.takeIf { it.shouldShowTopAppBar }?.let { destination ->
        when (destination.topAppBarType) {
            TopAppBarType.CENTER_ALIGNED -> JarvisSDKTopBarCenterAligned(destination, navigator, onDismiss)
            TopAppBarType.MEDIUM -> JarvisSDKTopBarMedium(destination, navigator, scrollBehavior, onDismiss)
            TopAppBarType.LARGE -> JarvisSDKTopBarLarge(destination, navigator, scrollBehavior, onDismiss)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSDKTopBarCenterAligned(
    destination: NavigationRoute,
    navigator: Navigator,
    onDismiss: () -> Unit
) {
    DSTopAppBar(
        titleRes = destination.titleTextId,
        navigationIcon = destination.navigationIcon,
        navigationIconContentDescription = destination.navigationIconContentDescription?.let {
            stringResource(id = it)
        },
        actionIcon = destination.actionIcon,
        actionIconContentDescription = destination.actionIconContentDescription?.let {
            stringResource(id = it)
        },
        logo = {
            Image(
                modifier = Modifier.size(DSJarvisTheme.dimensions.xl),
                painter = painterResource(R.drawable.ic_jarvis_logo),
                contentDescription = "Jarvis Logo"
            )
        },
        onActionClick = {
            destination.actionKey?.let { actionKey ->
                ActionRegistry.executeAction(actionKey)
            } ?: destination.onActionNavigate?.let { navigator.goTo(it) }
        },
        onBackClick = { navigator.goBack() },
        dismissable = destination.dismissable,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSDKTopBarLarge(
    destination: NavigationRoute,
    navigator: Navigator,
    scrollBehavior: TopAppBarScrollBehavior,
    onDismiss: () -> Unit
) {
    DSLargeTopAppBar(
        titleRes = destination.titleTextId,
        navigationIcon = destination.navigationIcon,
        navigationIconContentDescription = destination.navigationIconContentDescription?.let {
            stringResource(id = it)
        },
        actionIcon = destination.actionIcon,
        actionIconContentDescription = destination.actionIconContentDescription?.let {
            stringResource(id = it)
        },
        logo = {
            Image(
                modifier = Modifier.size(DSJarvisTheme.dimensions.xl),
                painter = painterResource(R.drawable.ic_jarvis_logo),
                contentDescription = "Jarvis Logo"
            )
        },
        onActionClick = {
            destination.actionKey?.let { actionKey ->
                ActionRegistry.executeAction(actionKey)
            } ?: destination.onActionNavigate?.let { navigator.goTo(it) }
        },
        onBackClick = { navigator.goBack() },
        dismissable = destination.dismissable,
        onDismiss = onDismiss,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisSDKTopBarMedium(
    destination: NavigationRoute,
    navigator: Navigator,
    scrollBehavior: TopAppBarScrollBehavior,
    onDismiss: () -> Unit
) {
    DSMediumTopAppBar(
        titleRes = destination.titleTextId,
        navigationIcon = destination.navigationIcon,
        navigationIconContentDescription = destination.navigationIconContentDescription?.let {
            stringResource(id = it)
        },
        actionIcon = destination.actionIcon,
        actionIconContentDescription = destination.actionIconContentDescription?.let {
            stringResource(id = it)
        },
        logo = {
            Image(
                modifier = Modifier.size(DSJarvisTheme.dimensions.xl),
                painter = painterResource(R.drawable.ic_jarvis_logo),
                contentDescription = "Jarvis Logo"
            )
        },
        onActionClick = {
            destination.actionKey?.let { actionKey ->
                ActionRegistry.executeAction(actionKey)
            } ?: destination.onActionNavigate?.let { navigator.goTo(it) }
        },
        onBackClick = { navigator.goBack() },
        dismissable = destination.dismissable,
        onDismiss = onDismiss,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun JarvisSDKBottomBar(
    currentDestination: NavigationRoute?,
    navigator: Navigator
) {
    val iconGradientTint = DSIconTint.Gradient(rememberJarvisPrimaryGradient())
    currentDestination?.takeIf { it.shouldShowBottomBar }?.let {
        DSNavigationBar(
            topCornerRadius = DSJarvisTheme.dimensions.l,
            tonalElevation = DSJarvisTheme.elevations.level4,
        ) {
            JarvisTopLevelDestinations.entries.forEachIndexed { _, item ->
                DSNavigationBarItem(
                    icon = {
                        DSIcon(
                            imageVector = item.icon,
                            contentDescription = item.iconContentDescription?.let { resId -> stringResource(resId) },
                            tint = iconGradientTint
                        )
                    },
                    selectedIcon = {
                        DSIcon(
                            imageVector = item.selectedIcon,
                            contentDescription = item.iconContentDescription?.let { resId -> stringResource(resId) },
                            tint = iconGradientTint
                        )
                    },
                    selected = isDestinationSelected(currentDestination, item.destination),
                    onClick = {
                        navigateToTab(navigator, item.destination)
                    }
                )
            }
        }
    }
}

/**
 * Handles tab navigation with intelligent stack management and history preservation.
 * If we're already in the same tab, pop to the root of that tab.
 * If switching tabs, preserve current tab's stack and restore target tab's stack.
 */
private fun navigateToTab(navigator: Navigator, tabDestination: NavigationRoute) {
    val currentDestination = navigator.currentDestination
    
    if (currentDestination != null && isDestinationSelected(currentDestination, tabDestination)) {
        // Already in this tab - pop to root of this tab
        if (currentDestination != tabDestination) {
            // Find the tab's root in the back stack and pop to it
            navigator.popTo(tabDestination)
        }
        // If already at root, do nothing (don't reset the tab)
    } else {
        // Switching to different tab - use tab-aware navigation to preserve history
        val currentTabKey = currentDestination?.let { navigator.getTabKey(it) }
        val targetTabKey = navigator.getTabKey(tabDestination)
        
        if (currentTabKey != null && targetTabKey != null) {
            navigator.switchToTab(tabDestination, currentTabKey, targetTabKey)
        } else {
            // Fallback to original behavior if tab keys can't be determined
            navigator.replace(tabDestination)
        }
    }
}

/**
 * Determines if a destination should be considered "selected" in the bottom navigation.
 * This handles the case where we're on a detail screen but want to highlight the parent tab.
 */
private fun isDestinationSelected(
    currentDestination: NavigationRoute?,
    tabDestination: NavigationRoute
): Boolean {
    if (currentDestination == null) return false
    
    // Direct match
    if (currentDestination == tabDestination) return true
    
    // Check if current destination belongs to the same feature module
    val currentRoute = currentDestination::class.qualifiedName ?: ""
    val tabRoute = tabDestination::class.qualifiedName ?: ""
    
    return when {
        // Inspector tab: select if we're on any inspector screen
        tabRoute.contains("inspector", ignoreCase = true) && 
        currentRoute.contains("inspector", ignoreCase = true) -> true
        
        // Preferences tab: select if we're on any preferences screen  
        tabRoute.contains("preferences", ignoreCase = true) && 
        currentRoute.contains("preferences", ignoreCase = true) -> true
        
        // Home tab: select if we're on any home screen
        tabRoute.contains("home", ignoreCase = true) && 
        currentRoute.contains("home", ignoreCase = true) -> true

        tabRoute.contains("settings", ignoreCase = true) &&
        currentRoute.contains("settings", ignoreCase = true) -> true
        
        else -> false
    }
}