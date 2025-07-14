package com.jarvis.demo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.jarvis.core.navigation.Destination
import com.jarvis.core.navigation.composable
import com.jarvis.core.navigation.createRoutePattern
import com.jarvis.core.navigation.navigation
import com.jarvis.demo.presentation.home.HomeDestinationsGraph
import com.jarvis.demo.presentation.home.HomeScreen
import com.jarvis.demo.presentation.ui.JarvisDemoAppState
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun JarvisDemoNavHost(
    appState: JarvisDemoAppState,
    startDestination: Destination,
    onShowSnackbar: suspend (String, String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = appState.navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        navigation<TopLevelDestination.Home>(
            startDestination = createRoutePattern<HomeDestinationsGraph.Home>(),
        ) {
            composable<HomeDestinationsGraph.Home> { HomeScreen(appState.navController::navigate) }
        }
    }
}