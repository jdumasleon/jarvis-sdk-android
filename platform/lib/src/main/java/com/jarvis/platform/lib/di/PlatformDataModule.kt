package com.jarvis.platform.lib.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jarvis.platform.api.analytics.Analytics
import com.jarvis.platform.api.crash.CrashReporter
import com.jarvis.platform.api.featureflags.FeatureFlags
import com.jarvis.platform.data.analytics.PostHogAnalytics
import com.jarvis.platform.data.crash.SentryCrashReporter
import com.jarvis.platform.data.featureflags.PostHogFeatureFlags
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extension for DataStore
private val Context.platformDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "platform_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformDataModule {
    
    @Binds
    @Singleton
    abstract fun bindAnalytics(
        postHogAnalytics: PostHogAnalytics
    ): Analytics
    
    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        sentryCrashReporter: SentryCrashReporter
    ): CrashReporter
    
    @Binds
    @Singleton
    abstract fun bindFeatureFlags(
        postHogFeatureFlags: PostHogFeatureFlags
    ): FeatureFlags
    
    companion object {
        @Provides
        @Singleton
        fun providePlatformDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.platformDataStore
    }
}