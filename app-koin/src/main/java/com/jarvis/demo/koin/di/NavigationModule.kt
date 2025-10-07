package com.jarvis.demo.koin.di

import androidx.navigation3.runtime.entry
import com.jarvis.core.internal.navigation.EntryProviderInstaller
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.demo.koin.presentation.home.HomeGraph
import com.jarvis.demo.koin.presentation.home.HomeScreen
import com.jarvis.demo.koin.presentation.inspector.InspectorGraph
import com.jarvis.demo.koin.presentation.inspector.InspectorScreen
import com.jarvis.demo.koin.presentation.preferences.PreferencesGraph
import com.jarvis.demo.koin.presentation.preferences.PreferencesScreen
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Navigation Module for Koin Demo App
 * Provides navigation dependencies and entry point installers
 */
val navigationModule = module {

    // Demo App Navigator (separate from Jarvis SDK navigator)
    single<Navigator>(named("demo_app")) {
        Navigator()
    }

    // Entry Provider Installers Set
    single<Set<EntryProviderInstaller>>(named("demo_entry_providers")) {
        setOf(
            // Home Entry Provider
            {
                entry<HomeGraph.Home> {
                    HomeScreen()
                }
            },
            // Inspector Entry Provider
            {
                entry<InspectorGraph.Inspector> {
                    InspectorScreen()
                }
            },
            // Preferences Entry Provider
            {
                entry<PreferencesGraph.Preferences> {
                    PreferencesScreen()
                }
            }
        )
    }
}