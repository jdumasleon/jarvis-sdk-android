package com.jarvis.api.di

import com.jarvis.core.navigation.JarvisSDKNavigator
import com.jarvis.core.navigation.Navigator
import com.jarvis.core.navigation.routes.JarvisSDKHomeGraph
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object JarvisSDKModule {
    
    @Provides
    @ActivityRetainedScoped
    @JarvisSDKNavigator
    fun provideJarvisSDKNavigator(): Navigator {
        return Navigator().apply {
            initialize(JarvisSDKHomeGraph.JarvisHome)
        }
    }
}