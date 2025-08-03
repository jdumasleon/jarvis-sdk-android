package com.jarvis.demo.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.jarvis.core.designsystem.component.DSBackground
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.core.designsystem.component.DSDrawer
import com.jarvis.core.designsystem.component.DSDrawerValue
import com.jarvis.core.designsystem.component.DSGradientBackground
import com.jarvis.core.designsystem.component.DSTopAppBar
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.component.rememberCardDrawerState
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSGradientColors
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.LocalDSGradientColors
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.demo.R
import com.jarvis.demo.presentation.home.HomeDestinations
import com.jarvis.demo.presentation.inspector.InspectorDestinations
import com.jarvis.demo.presentation.navigation.JarvisDemoNavDisplay
import com.jarvis.demo.presentation.navigation.TopLevelDestination
import com.jarvis.demo.presentation.preferences.PreferencesDestinations
import kotlinx.coroutines.launch

//region .: Jarvis Demo App :.
@Composable
fun JarvisDemoApp(
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    modifier: Modifier = Modifier
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
    val drawerState = rememberCardDrawerState(DSDrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(navigator.currentDestination) }

    DSDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination.destination
                    // Navigate using the modular navigator
                    when (destination) {
                        TopLevelDestination.HOME -> navigator.goTo(HomeDestinations.Home)
                        TopLevelDestination.INSPECTOR -> navigator.goTo(InspectorDestinations.Inspector)
                        TopLevelDestination.PREFERENCES -> navigator.goTo(PreferencesDestinations.Preferences)
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
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
    currentDestination: NavigationRoute?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DSJarvisTheme.spacing.m)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.l)
        ) {
            DSText(
                text = stringResource(R.string.app_name),
                style = DSJarvisTheme.typography.heading.heading5,
                fontWeight = FontWeight.Bold,
                color = DSJarvisTheme.colors.primary.primary60
            )
            
            DSText(
                text = stringResource(R.string.jarvis_description),
                style = DSJarvisTheme.typography.body.medium,
                color = DSJarvisTheme.colors.neutral.neutral60
            )
            
            DSText(
                text = stringResource(R.string.version),
                style = DSJarvisTheme.typography.body.small,
                color = DSJarvisTheme.colors.neutral.neutral40
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = DSJarvisTheme.spacing.m),
            color = DSJarvisTheme.colors.neutral.neutral20
        )
        
        // Navigation items
        DSText(
            text = "Tools",
            style = DSJarvisTheme.typography.body.small,
            fontWeight = FontWeight.Bold,
            color = DSJarvisTheme.colors.neutral.neutral40,
            modifier = Modifier.padding(horizontal = DSJarvisTheme.spacing.m, vertical = DSJarvisTheme.spacing.s)
        )
        
        TopLevelDestination.entries.forEach { destination ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes),
                        modifier = Modifier.size(DSJarvisTheme.dimensions.l)
                    )
                },
                label = {
                    DSText(
                        text = stringResource(destination.titleRes),
                        style = DSJarvisTheme.typography.body.medium
                    )
                },
                selected = currentDestination == destination.destination,
                onClick = { onDestinationSelected(destination) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = DSJarvisTheme.colors.primary.primary100,
                    selectedIconColor = DSJarvisTheme.colors.primary.primary60,
                    selectedTextColor = DSJarvisTheme.colors.primary.primary60,
                    unselectedIconColor = DSJarvisTheme.colors.neutral.neutral60,
                    unselectedTextColor = DSJarvisTheme.colors.neutral.neutral80
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
            navigationIcon = DSIcons.Menu,
            navigationIconContentDescription = "Menu",
            actionIcon = destination.actionIcon,
            actionIconContentDescription = destination.actionIconContentDescription?.let {
                stringResource(id = it)
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
            onActionClick = {
                // Handle action based on current screen
                when (destination) {
                    is InspectorDestinations.Inspector -> {
                        // Add random API call - we'll need to get the viewModel
                    }
                    else -> {
                        destination.onActionNavigate?.let { navigator.goTo(it) }
                    }
                }
            },
            onBackClick = onMenuClick // Use menu click instead of back
        )
    }
}

//endregion