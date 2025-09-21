package com.jarvis.internal.feature.settings.di

import androidx.navigation3.runtime.entry
import com.jarvis.core.navigation.EntryProviderInstaller
import com.jarvis.core.navigation.JarvisSDKNavigator
import com.jarvis.core.navigation.Navigator
import com.jarvis.core.navigation.routes.JarvisSDKInspectorGraph
import com.jarvis.core.navigation.routes.JarvisSDKPreferencesGraph
import com.jarvis.core.navigation.routes.JarvisSDKSettingsGraph
import com.jarvis.internal.feature.settings.presentation.SettingsRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
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