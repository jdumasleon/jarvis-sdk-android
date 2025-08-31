package com.jarvis.demo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.navigation.Navigator
import com.jarvis.core.presentation.navigation.ModularNavDisplay

@Composable
fun JarvisDemoNavDisplay(
    modifier: Modifier = Modifier,
    onCurrentDestinationChanged: (NavigationRoute) -> Unit,
    navigator: Navigator,
    entryProviderBuilders: Set<@JvmSuppressWildcards EntryProviderInstaller>
) {
    ModularNavDisplay(
        modifier = modifier,
        navigator = navigator,
        entryProviderBuilders = entryProviderBuilders,
        onCurrentDestinationChanged = { destination ->
            onCurrentDestinationChanged(destination)
        }
    )
}