package com.jarvis.demo.di

import com.jarvis.core.presentation.navigation.Navigator
import com.jarvis.demo.presentation.home.HomeDestinations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object AppModule {

    @Provides
    @ActivityRetainedScoped
    fun provideNavigator() : Navigator = Navigator(startDestination = HomeDestinations.Home)
}
