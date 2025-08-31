package com.jarvis.demo.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.jarvis.core.designsystem.component.DSBackground
import com.jarvis.core.designsystem.component.DSDrawer
import com.jarvis.core.designsystem.component.DSDrawerValue
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSTopAppBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.rememberDSDrawerState
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.navigation.ActionRegistry
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.navigation.Navigator
import com.jarvis.demo.R
import com.jarvis.demo.presentation.home.HomeGraph
import com.jarvis.demo.presentation.inspector.InspectorGraph
import com.jarvis.demo.presentation.navigation.JarvisDemoNavDisplay
import com.jarvis.demo.presentation.navigation.TopLevelDestination
import com.jarvis.demo.presentation.preferences.PreferencesGraph
import kotlinx.coroutines.launch

//region .: Jarvis Demo App :.
@Composable
fun JarvisDemoApp(
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    modifier: Modifier = Modifier,
) {
    DSBackground(modifier = modifier) {
        val snackBarHostState = remember { SnackbarHostState() }

        JarvisDemoApp(
            navigator = navigator,
            entryProviderBuilders = entryProviderBuilders,
            snackBarHostState = snackBarHostState,
        )
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
internal fun JarvisDemoApp(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    snackBarHostState: SnackbarHostState,
) {
    val drawerState = rememberDSDrawerState(DSDrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentDestination by remember(navigator.currentDestination) { mutableStateOf(navigator.currentDestination) }

    DSDrawer(
        drawerState = drawerState,
        drawerBackgroundColor = DSJarvisTheme.colors.extra.white,
        contentCornerSize = DSJarvisTheme.dimensions.m,
        drawerContent = {
            DrawerContent(
                currentDestination = currentDestination,
                onClose = { scope.launch { drawerState.close() } },
                onDestinationSelected = { destination ->
                    currentDestination = destination.destination
                    when (destination) {
                        TopLevelDestination.HOME -> navigator.goTo(HomeGraph.Home)
                        TopLevelDestination.INSPECTOR -> navigator.goTo(InspectorGraph.Inspector)
                        TopLevelDestination.PREFERENCES -> navigator.goTo(PreferencesGraph.Preferences)
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            containerColor = DSJarvisTheme.colors.extra.background,
            topBar = { 
                JarvisTopBar(
                    currentDestination = currentDestination,
                    navigator = navigator,
                    onMenuClick = {
                        scope.launch { 
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackBarHostState) }
        ) { padding ->
            JarvisDemoNavDisplay(
                navigator = navigator,
                entryProviderBuilders = entryProviderBuilders,
                modifier = Modifier.padding(padding),
                onCurrentDestinationChanged = { currentDestination = it }
            )
        }
    }
}
//endregion

//region .: Private components :.
@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    currentDestination: NavigationRoute?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    onClose: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = DSJarvisTheme.spacing.m,
                bottom = DSJarvisTheme.spacing.m,
                start = DSJarvisTheme.spacing.m,
            )
    ) {
        // Header
        Column(
            modifier = Modifier.padding(
                top = DSJarvisTheme.spacing.l,
                bottom = DSJarvisTheme.spacing.l,
                start = DSJarvisTheme.spacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = DSJarvisTheme.spacing.m)
                        .size(DSJarvisTheme.dimensions.xxxxxxxxl)
                        .clip(CircleShape)
                        .background(DSJarvisTheme.colors.extra.background),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(com.jarvis.core.designsystem.R.drawable.ic_jarvis_logo),
                        contentDescription = "Jarvis Logo",
                        modifier = Modifier.size(DSJarvisTheme.dimensions.xxxxxxxxxl)
                    )
                }

                DSIcon(
                    imageVector = DSIcons.Rounded.close,
                    contentDescription = "Drawer close",
                    tint = DSJarvisTheme.colors.extra.black,
                    modifier = Modifier
                        .padding(vertical = DSJarvisTheme.spacing.m)
                        .clickable(onClick = { onClose() })
                )
            }

            DSText(
                text = stringResource(R.string.app_name),
                style = DSJarvisTheme.typography.heading.medium,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.extra.black
            )

            DSText(
                text = stringResource(R.string.jarvis_description),
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral100
            )

            DSText(
                text = stringResource(R.string.version),
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral80
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m),
            color = DSJarvisTheme.colors.neutral.neutral40
        )

        Spacer(modifier = Modifier.height(DSJarvisTheme.spacing.m))

        // Navigation items
        DSText(
            text = stringResource(R.string.tools),
            style = DSJarvisTheme.typography.body.small,
            fontWeight = FontWeight.Bold,
            color = DSJarvisTheme.colors.extra.black,
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m, vertical = DSJarvisTheme.spacing.s)
        )

        TopLevelDestination.entries.forEach { destination ->
            NavigationDrawerItem(
                icon = {
                    DSIcon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes),
                        modifier = Modifier.size(DSJarvisTheme.dimensions.l),
                        tint = if (currentDestination == destination.destination) DSJarvisTheme.colors.primary.primary60  else DSJarvisTheme.colors.neutral.neutral60,
                    )
                },
                label = {
                    DSText(
                        text = stringResource(destination.titleRes),
                        style = DSJarvisTheme.typography.body.medium,
                        color = if (currentDestination == destination.destination) DSJarvisTheme.colors.primary.primary60 else DSJarvisTheme.colors.neutral.neutral60,
                    )
                },
                selected = currentDestination == destination.destination,
                onClick = { onDestinationSelected(destination) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color.Transparent,
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = DSJarvisTheme.colors.extra.black,
                    unselectedIconColor = DSJarvisTheme.colors.neutral.neutral80,
                ),
                modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.xs)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun JarvisTopBar(
    currentDestination: NavigationRoute?,
    navigator: Navigator,
    onMenuClick: () -> Unit
) {
    currentDestination?.takeIf { it.shouldShowTopAppBar }?.let { destination ->
        DSTopAppBar(
            titleRes = destination.titleTextId,
            navigationIcon = DSIcons.menu,
            navigationIconContentDescription = "Menu",
            actionIcon = destination.actionIcon,
            actionIconContentDescription = destination.actionIconContentDescription?.let {
                stringResource(id = it)
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            onActionClick = {
                destination.actionKey?.let { actionKey ->
                    ActionRegistry.executeAction(actionKey)
                } ?: destination.onActionNavigate?.let { navigator.goTo(it) }
            },
            onBackClick = onMenuClick // Use menu click instead of back
        )
    }
}

//endregion