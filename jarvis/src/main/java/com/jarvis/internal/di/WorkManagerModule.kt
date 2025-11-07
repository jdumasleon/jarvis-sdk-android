package com.jarvis.internal.di

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.hilt.work.HiltWorkerFactory
import com.jarvis.internal.data.work.NetworkCleanupScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for WorkManager dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideNetworkCleanupScheduler(
        @ApplicationContext context: Context,
        workerFactory: HiltWorkerFactory
    ): NetworkCleanupScheduler {
        return NetworkCleanupScheduler(context, workerFactory)
    }
}
