package com.jarvis.features.home.lib.di.presentation

import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.features.home.lib.navigation.JarvisSDKHomeGraph
import com.jarvis.features.home.presentation.ui.HomeRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object JarvisHomePresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller =
        {
            entry<JarvisSDKHomeGraph.JarvisHome> {
                HomeRoute()
            }
        }
}