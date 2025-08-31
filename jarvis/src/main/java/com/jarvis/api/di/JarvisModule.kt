package com.jarvis.api.di

import com.jarvis.core.navigation.EntryProviderInstaller
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent
::class)
interface JarvisSDKEntryPoint {
    fun entryProviderBuilders(): Set<@JvmSuppressWildcards EntryProviderInstaller>
}