package com.jarvis.internal.navigation

import androidx.annotation.RestrictTo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.presentation.navigation.ModularNavDisplay
import com.jarvis.core.internal.navigation.NavigationRoute
import com.jarvis.core.internal.navigation.Navigator

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JarvisSDKNavDisplay(
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