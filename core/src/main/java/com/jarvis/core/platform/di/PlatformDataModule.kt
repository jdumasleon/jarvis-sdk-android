package com.jarvis.core.platform.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jarvis.core.platform.analytics.Analytics
import com.jarvis.core.platform.crash.CrashReporter
import com.jarvis.core.platform.featureflags.FeatureFlags
import com.jarvis.core.platform.analytics.PostHogAnalytics
import com.jarvis.core.platform.crash.SentryCrashReporter
import com.jarvis.core.platform.featureflags.PostHogFeatureFlags
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