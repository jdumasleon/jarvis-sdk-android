package com.jarvis.features.home.lib.di.presentation

import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import com.jarvis.features.home.presentation.ui.HomeRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class HomePresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller =
        {
            entry<JarvisSDKHomeGraph.JarvisHome> {
                HomeRoute()
            }
        }
}