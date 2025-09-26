package com.jarvis.internal.feature.settings.di

import androidx.annotation.RestrictTo

import androidx.navigation3.runtime.entry
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.JarvisSDKNavigator
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.core.internal.navigation.routes.JarvisSDKSettingsGraph
import com.jarvis.internal.feature.settings.presentation.SettingsRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SettingsPresentationModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(@JarvisSDKNavigator navigator: Navigator): EntryProviderInstaller =
        {
            entry<JarvisSDKSettingsGraph.JarvisSettings> {
                SettingsRoute(
                    onNavigateToPreferences = {
                        navigator.goTo(JarvisSDKPreferencesGraph.JarvisPreferences)
                    },
                    onNavigateToInspector = {
                        navigator.goTo(JarvisSDKInspectorGraph.JarvisInspectorTransactions)
                    },
                    onNavigateToLogging = {

                    }
                )
            }
        }
}