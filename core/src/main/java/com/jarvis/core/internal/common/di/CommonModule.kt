package com.jarvis.core.internal.common.di

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.common.network.NetworkMonitor
import com.jarvis.core.internal.common.network.NetworkMonitorManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class DataModule {

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: NetworkMonitorManager,
    ): NetworkMonitor
}