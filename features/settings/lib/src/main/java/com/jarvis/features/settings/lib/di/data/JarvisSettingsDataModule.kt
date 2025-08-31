package com.jarvis.features.settings.lib.di.data

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class JarvisSettingsDataModule {
    // Settings repository binding is now handled in SettingsDataModule
    // This module is kept for potential future bindings specific to the lib layer
}