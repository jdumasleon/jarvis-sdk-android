package com.jarvis.api.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.jarvis.api.di.JarvisOverlayEntryPoint
import com.jarvis.api.ui.navigation.JarvisSDKNavDisplay
import com.jarvis.api.ui.navigation.JarvisTopLevelDestinations
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSLargeTopAppBar
import com.jarvis.core.designsystem.component.DSMediumTopAppBar
import com.jarvis.core.designsystem.component.DSNavigationBar
import com.jarvis.core.designsystem.component.DSNavigationBarItem
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.DSTopAppBar
import com.jarvis.core.designsystem.component.DynamicOrbCanvas
import com.jarvis.core.designsystem.component.StateConfig
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.presentation.navigation.ActionRegistry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.core.presentation.navigation.TopAppBarType
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import dagger.hilt.android.EntryPointAccessors

/**
 * Jarvis SDK overlay that displays as a full-screen dialog
 * This provides completely independent navigation without affecting the host app
 */
class JarvisOverlay(private val context: Context) {

    private lateinit var entryProviderBuilders: Set<EntryProviderInstaller>

    private var composeView: ComposeView? = null
    private var isShowing = false

    private var navigator: Navigator = Navigator()

    fun show(route: NavigationRoute) {
        val activity = context as? Activity ?: return

        val ep = EntryPointAccessors.fromActivity(activity, JarvisOverlayEntryPoint::class.java)
        entryProviderBuilders = ep.entryProviderBuilders()

        if (isShowing) {
            navigator.replace(route)
            return
        } else {
            navigator.initialize(route)
        }

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(activity as androidx.lifecycle.LifecycleOwner)
            setViewTreeViewModelStoreOwner(activity as androidx.lifecycle.ViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(activity as androidx.savedstate.SavedStateRegistryOwner)

            setContent {
                val darkTheme = isSystemInDarkTheme()
                DSJarvisTheme(darkTheme = darkTheme) {
                    JarvisOverlayContent(
                        navigator = navigator,
                        entryProviderBuilders = entryProviderBuilders,
                        onDismiss = { dismiss() }
                    )
                }
            }
        }

        activity.addContentView(
            view,
            android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        composeView = view
        isShowing = true
    }

    fun dismiss() {
        if (!isShowing) return

        navigator.clear()
        composeView?.let { v ->
            (v.parent as? android.view.ViewGroup)?.removeView(v)
        }
        composeView = null
        isShowing = false
    }

    fun isShowing(): Boolean = isShowing
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JarvisOverlayContent(
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    onDismiss: () -> Unit = {}
) {
    var currentDestination by remember { mutableStateOf(navigator.currentDestination) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

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
fun JarvisSDKTopBarCenterAligned(destination: NavigationRoute, navigator: Navigator, onDismiss: () -> Unit) {
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
            DSIcon(
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
            DSIcon(
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
            DSIcon(
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

@Composable
private fun JarvisSDKBottomBar(
    currentDestination: NavigationRoute?,
    navigator: Navigator
) {
    currentDestination?.takeIf { it.shouldShowBottomBar }?.let {
        DSNavigationBar(
            topCornerRadius = DSJarvisTheme.dimensions.l,
            tonalElevation = DSJarvisTheme.elevations.level4
        ) {
            JarvisTopLevelDestinations.entries.forEachIndexed { _, item ->
                DSNavigationBarItem(
                    icon = {
                        DSIcon(
                            imageVector = item.icon,
                            contentDescription = item.iconContentDescription?.let { resId -> stringResource(resId) },
                            tint = DSJarvisTheme.colors.neutral.neutral100
                        )
                    },
                    selectedIcon = {
                        DSIcon(
                            imageVector = item.selectedIcon,
                            contentDescription = item.iconContentDescription?.let { resId -> stringResource(resId) },
                            tint = DSJarvisTheme.colors.primary.primary60
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
        
        else -> false
    }
}