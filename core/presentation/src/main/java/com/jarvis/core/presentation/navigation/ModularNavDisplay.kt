package com.jarvis.core.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.navigation.Navigator

/**
 * Modular navigation display component using simple composable mapping
 * Uses the modular navigation pattern with Navigator and simple entry provider
 */
@Composable
fun ModularNavDisplay(
    navigator: Navigator,
    entryProviderBuilders: Set<EntryProviderInstaller>,
    modifier: Modifier = Modifier,
    onCurrentDestinationChanged: ((NavigationRoute) -> Unit)? = null
) {
    val provider = remember(entryProviderBuilders, navigator) {
        entryProvider {
            entryProviderBuilders.forEach { builder ->
                this.builder(navigator)
            }
        }
    }

    val currentDestination = navigator.currentDestination
    
    // Notify about destination changes
    LaunchedEffect(currentDestination) {
        currentDestination?.let { onCurrentDestinationChanged?.invoke(it) }
    }

    NavDisplay(
        modifier = modifier.fillMaxSize(),
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = provider
    )
}