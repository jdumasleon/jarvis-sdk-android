package com.jarvis.core.common.di

import com.jarvis.core.common.network.NetworkMonitor
import com.jarvis.core.common.network.NetworkMonitorManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: NetworkMonitorManager,
    ): NetworkMonitor
}