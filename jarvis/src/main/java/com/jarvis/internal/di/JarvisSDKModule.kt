package com.jarvis.internal.di

import androidx.annotation.RestrictTo

import com.jarvis.core.internal.navigation.JarvisSDKNavigator
import com.jarvis.core.internal.navigation.Navigator
import com.jarvis.core.internal.navigation.routes.JarvisSDKHomeGraph
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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