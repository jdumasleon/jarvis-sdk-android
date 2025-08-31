package com.jarvis.demo.presentation.home

import androidx.navigation3.runtime.entry
import com.jarvis.core.navigation.EntryProviderInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object HomeModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller =
        {
            entry<HomeGraph.Home> {
                HomeScreen()
            }
        }
}