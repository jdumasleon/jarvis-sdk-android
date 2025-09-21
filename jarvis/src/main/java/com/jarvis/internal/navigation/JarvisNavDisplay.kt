package com.jarvis.internal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.ModularNavDisplay
import com.jarvis.core.navigation.NavigationRoute
import com.jarvis.core.navigation.Navigator

@Composable
internal fun JarvisSDKNavDisplay(
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