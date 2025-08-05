package com.jarvis.demo.di

import android.content.Context
import com.jarvis.api.core.JarvisSDK
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JarvisSDKModule {
    
    @Provides
    @Singleton
    fun provideJarvisSDK(
        @ApplicationContext context: Context
    ): JarvisSDK {
        return JarvisSDK(context)
    }
}