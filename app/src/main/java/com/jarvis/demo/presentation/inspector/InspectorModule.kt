package com.jarvis.demo.presentation.inspector

import androidx.navigation3.runtime.entry
import com.jarvis.core.presentation.navigation.EntryProviderInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object InspectorModule {

    @IntoSet
    @Provides
    fun provideEntryProviderInstaller() : EntryProviderInstaller = {
        entry<InspectorDestinations.Inspector>{
            InspectorScreen()
        }
    }
}