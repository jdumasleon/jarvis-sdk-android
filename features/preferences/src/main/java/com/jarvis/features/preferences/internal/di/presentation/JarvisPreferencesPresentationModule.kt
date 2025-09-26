@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.features.preferences.internal.di.presentation

import androidx.annotation.RestrictTo
import androidx.navigation3.runtime.entry
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.features.preferences.internal.presentation.PreferencesRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
internal object JarvisPreferencesPresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller =
        {
            entry<JarvisSDKPreferencesGraph.JarvisPreferences> {
                PreferencesRoute()
            }
        }
}