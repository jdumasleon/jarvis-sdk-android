package com.jarvis.api.di

import com.jarvis.core.presentation.navigation.JarvisEntryProviderInstaller
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@EntryPoint
@InstallIn(ActivityRetainedComponent::class)
interface JarvisOverlayEntryPoint {
    fun entryProviderBuilders(): Set<@JvmSuppressWildcards JarvisEntryProviderInstaller>
}