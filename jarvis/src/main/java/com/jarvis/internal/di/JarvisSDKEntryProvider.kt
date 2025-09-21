package com.jarvis.internal.di

import com.jarvis.core.navigation.EntryProviderInstaller
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface JarvisSDKEntryProvider {
    fun entryProviderBuilders(): Set<@JvmSuppressWildcards EntryProviderInstaller>
}