@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.internal.di

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.navigation.EntryProviderInstaller
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface JarvisSDKEntryProvider {
    fun entryProviderBuilders(): Set<@JvmSuppressWildcards EntryProviderInstaller>
}