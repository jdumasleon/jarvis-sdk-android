package com.jarvis.internal.feature.home.di

import androidx.annotation.RestrictTo

import androidx.navigation3.runtime.entry
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.routes.JarvisSDKHomeGraph
import com.jarvis.internal.feature.home.presentation.HomeRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object HomePresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller =
        {
            entry<JarvisSDKHomeGraph.JarvisHome> {
                HomeRoute()
            }
        }
}