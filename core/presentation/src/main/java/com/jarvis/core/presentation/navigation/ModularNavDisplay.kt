package com.jarvis.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

/**
 * Modular navigation display component using simple composable mapping
 * Uses the modular navigation pattern with Navigator and simple entry provider
 */
@Composable
fun ModularNavDisplay(
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>,
    modifier: Modifier = Modifier,
    onCurrentDestinationChanged: ((Any) -> Unit)? = null
) {
    val currentDestination = navigator.currentDestination
    
    // Notify about destination changes
    LaunchedEffect(currentDestination) {
        currentDestination?.let { destination ->
            onCurrentDestinationChanged?.invoke(destination)
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entryProviderBuilders.forEach { builder -> this.builder() }
        }
    )
}