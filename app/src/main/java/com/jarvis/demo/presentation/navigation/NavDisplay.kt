package com.jarvis.demo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.ModularNavDisplay
import com.jarvis.core.presentation.navigation.NavigationRoute
import com.jarvis.core.presentation.navigation.Navigator

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
            onCurrentDestinationChanged(destination as NavigationRoute)
        }
    )
}