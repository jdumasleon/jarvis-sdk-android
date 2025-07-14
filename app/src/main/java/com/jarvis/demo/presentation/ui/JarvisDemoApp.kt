package com.jarvis.demo.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Short
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import com.jarvis.core.designsystem.component.DSBackground
import com.jarvis.core.designsystem.component.DSGradientBackground
import com.jarvis.core.designsystem.component.DSTopAppBar
import com.jarvis.core.designsystem.theme.DSGradientColors
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.LocalDSGradientColors
import com.jarvis.core.navigation.Destination
import com.jarvis.demo.presentation.navigation.JarvisDemoNavHost
import com.jarvis.demo.presentation.navigation.TopLevelDestination

@Composable
fun JarvisDemoApp(
    appState: JarvisDemoAppState,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    val shouldShowGradientBackground = appState.currentDestinations == TopLevelDestination.Home

    DSBackground(modifier = modifier) {
        DSGradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalDSGradientColors.current
            } else {
                DSGradientColors()
            },
        ) {
            val snackBarHostState = remember { SnackbarHostState() }

            JarvisDemoApp(
                appState = appState,
                startDestination = startDestination,
                snackBarHostState = snackBarHostState,
            )
        }
    }
}

@Composable
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
internal fun JarvisDemoApp(
    appState: JarvisDemoAppState,
    startDestination: Destination,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.semantics {
            testTagsAsResourceId = true
        },
        containerColor = Color.Transparent,
        contentColor = DSJarvisTheme.colors.neutral.neutral100,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            // Show the top app bar on top level destinations.
            val destination = appState.currentDestinations
            var shouldShowTopAppBar = false

            if (destination != null) {
                shouldShowTopAppBar = true
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    onActionClick = destination.onActionClick,
                    onNavigationClick = destination.onNavigationClick,
                )
            }

            Box(
                // Workaround for https://issuetracker.google.com/338478720
                modifier = Modifier.consumeWindowInsets(
                    if (shouldShowTopAppBar) {
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    } else {
                        WindowInsets(0, 0, 0, 0)
                    },
                ),
            ) {
                JarvisDemoNavHost(
                    appState = appState,
                    startDestination = startDestination,
                    onShowSnackbar = { message, action ->
                        snackBarHostState.showSnackbar(
                            message = message,
                            actionLabel = action,
                            duration = Short,
                        ) == ActionPerformed
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DSJarvisTheme {
        JarvisDemoApp(
            appState = rememberJarvisDemoAppState(),
            startDestination = TopLevelDestination.Home.destination,
        )
    }
}