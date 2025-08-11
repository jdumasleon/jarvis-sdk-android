package com.jarvis.api.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jarvis.core.designsystem.component.DSIcon
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
                DSJarvisTheme {
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            JarvisSDKTopBar(
                currentDestination = currentDestination,
                scrollBehavior = scrollBehavior,
                navigator = navigator,
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
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigator: Navigator,
    onDismiss: () -> Unit = {}
) {
    currentDestination?.takeIf { it.shouldShowTopAppBar }?.let { destination ->
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
                DynamicOrbCanvas(
                    config = StateConfig(
                        name = "Initializing",
                        colors = listOf(
                            DSJarvisTheme.colors.primary.primary40,
                            DSJarvisTheme.colors.primary.primary60,
                            DSJarvisTheme.colors.primary.primary80
                        ),
                        speed = 1.2f,
                        morphIntensity = 2.0f
                    ),
                    modifier = Modifier.fillMaxSize()
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
                        )
                    },
                    selectedIcon = {
                        DSIcon(
                            imageVector = item.selectedIcon,
                            contentDescription = item.iconContentDescription?.let { resId -> stringResource(resId) },
                        )
                    },
                    selected = currentDestination == item.destination,
                    onClick = {
                        navigator.replace(item.destination)
                    }
                )
            }
        }
    }
}