package com.jarvis.features.preferences.lib.di.presentation

import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.features.preferences.lib.navigation.JarvisSDKPreferencesGraph
import com.jarvis.features.preferences.presentation.ui.PreferencesRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object JarvisPreferencesPresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(): EntryProviderInstaller =
        {
            entry<JarvisSDKPreferencesGraph.JarvisPreferences> {
                PreferencesRoute()
            }
        }
}