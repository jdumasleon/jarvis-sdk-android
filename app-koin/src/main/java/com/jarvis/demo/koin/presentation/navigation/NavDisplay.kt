package com.jarvis.demo.koin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.presentation.navigation.ModularNavDisplay

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